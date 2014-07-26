/*
 * Copyright 2014 Sebastian Kruse, Thorben Lindhauer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.unipotsdam.hpi.indexing;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

import de.unipotsdam.hpi.util.BitSignatureUtil;
import de.unipotsdam.hpi.util.FileUtils;

/**
 * Stores the index in a number of blocks. Index is sorted by keys ascending.
 * 
 * @author Thorben
 * 
 */
public abstract class AbstractBlockBasedIndex<T extends LinkedBlock<T>> implements Index, Iterable<IndexPair> {

	private static final long serialVersionUID = -8071670886248402811L;

	protected T firstBlock;
	transient protected Path basePath;
	protected String basePathString;
	protected int keySize;
	protected int blockSize;
	protected int blockIdCounter = 0;
	public static final double INITIAL_LOAD_FACTOR = 0.75d;

	/**
	 * @param basePath
	 * @param keySize
	 *            in longs
	 * @param blockSize
	 *            number of items per block
	 */
	public AbstractBlockBasedIndex(Path basePath, int keySize, int blockSize) {
		this.basePath = basePath;
		this.basePathString = basePath.toString();
		this.keySize = keySize;
		this.blockSize = blockSize;
	}

	public void insertElement(long[] key, int value) throws IOException {
		insertElement(new IndexPair(key, value));
	}
	
	public abstract void insertElement(IndexPair pair) throws IOException;

	public abstract void deleteElement(long[] key);

	public abstract IndexPair[] getNearestNeighboursPairs(long[] key, int beamRadius);
	
	public abstract int[] getNearestNeighboursElementIds(long[] key, int beamRadius);

//	public abstract void bulkLoad(IndexPair[] keyValuePairs);

	public abstract int getElement(long[] key);

	/**
	 * Find the block that potentially might contain the entry associated with
	 * the given key.
	 */
	protected T getBlockFor(long[] key) {
		// Scan the blocks until we find a block that has a greater start key
		// than the given key.
		// If that does not happen, we need to return the last block.
		T lastValidBlock = null;
		for (T curBlock = firstBlock; curBlock != null; curBlock = curBlock
				.getNextBlock()) {
			int comparison = BitSignatureUtil.COMPARATOR.compare(key,
					curBlock.getStartKey());
			if (comparison == 0)
				// shortcut for exact matches
				return curBlock;
			else if (comparison > 0)
				// search key is still greater than
				lastValidBlock = curBlock;
			else
				// search key is smaller
				break;
		}
		return lastValidBlock;
	}

	public int size() {
		int size = 0;
		for (T curBlock = firstBlock; curBlock != null; curBlock = curBlock
				.getNextBlock()) {
			size += curBlock.getSize();
		}
		return size;
	}

	public abstract Iterator<IndexPair> iterator();

	public void close() throws IOException {
		for (T curBlock = firstBlock; curBlock != null; curBlock = curBlock.getNextBlock()) {
			curBlock.close();
		}
	}

	public void recover() {
		this.basePath = FileUtils.toPath(basePathString);
		
		for (T block = firstBlock; block != null; block = block.getNextBlock()) {
			block.recover();
		}
	}
	
	public void bulkLoad(IndexPair[] keyValuePairs) {
    try {
      int pairsPerBlock = (int) Math
          .ceil(INITIAL_LOAD_FACTOR * blockSize);
      // ceil keyValuePairs.length / pairsPerBlock
      int numBlocks = (keyValuePairs.length + pairsPerBlock - 1)
          / pairsPerBlock;
      T currentBlock = null;
      T lastBlock = null;
      int offset = 0;
      int remainingPairs = keyValuePairs.length;
      for (int i = 0; i < numBlocks; i++) {
        currentBlock = createNewBlock();

        if (firstBlock == null) {
          firstBlock = currentBlock;
        }

        if (lastBlock != null) {
          lastBlock.setNextBlock(currentBlock);
          currentBlock.setPreviousBlock(lastBlock);
        }
        lastBlock = currentBlock;

        int pairsToWrite = Math.min(remainingPairs, pairsPerBlock);
        bulkLoadBlock(currentBlock, keyValuePairs, offset, pairsToWrite);
        remainingPairs -= pairsToWrite;
        offset += pairsToWrite;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
	
	protected abstract T createNewBlock() throws IOException;
	
	protected abstract void bulkLoadBlock(T block, IndexPair[] pairs, int offset, int pairsToWrite) throws IOException;

}
