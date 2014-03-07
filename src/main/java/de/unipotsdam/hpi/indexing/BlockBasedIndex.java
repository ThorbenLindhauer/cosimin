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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.unipotsdam.hpi.util.BitSignatureUtil;
import de.unipotsdam.hpi.util.FileUtils;

/**
 * Stores the index in a number of blocks. Index is sorted by keys ascending.
 * 
 * @author Thorben
 * 
 */
public class BlockBasedIndex implements Index, Iterable<IndexPair> {

	private static final long serialVersionUID = -8071670886248402811L;

	private LinkedBlock firstBlock;
	transient private Path basePath;
	private String basePathString;
	private int keySize;
	private int blockSize;
	private int blockIdCounter = 0;
	public static final double INITIAL_LOAD_FACTOR = 0.75d;

	/**
	 * @param basePath
	 * @param keySize
	 *            in longs
	 * @param blockSize
	 *            number of items per block
	 */
	public BlockBasedIndex(Path basePath, int keySize, int blockSize) {
		this.basePath = basePath;
		this.basePathString = basePath.toString();
		this.keySize = keySize;
		this.blockSize = blockSize;
	}

	public void insertElement(long[] key, int value) throws IOException {
		insertElement(new IndexPair(key, value));
	}

	public void insertElement(IndexPair pair) throws IOException {
		// If there is no block yet, allocate one and insert the element.
		if (firstBlock == null) {
			firstBlock = createNewBlock();
			firstBlock.insertElement(pair);
		} else {
			LinkedBlock block = getBlockFor(pair.getBitSignature());
			if (block == null) {
				// If there is no block that might contain the pair, all blocks
				// have a greater start key.
				// So add to the first block.
				block = firstBlock;
			}
			if (block.getSize() < block.getCapacity()) {
				block.insertElement(pair);
			} else {
				// Split the block and add the elements:
				// 1. Create a new block and link it right after the current
				// block.
				LinkedBlock newBlock = createNewBlock();
				newBlock.setPreviousBlock(block);
				newBlock.setNextBlock(block.getNextBlock());
				block.setNextBlock(newBlock);
				if (newBlock.getNextBlock() != null) {
					newBlock.getNextBlock().setPreviousBlock(newBlock);
				}

				// 2. Retrieve all elements and spread them over the new blocks.
				IndexPair[] pairs = block.getElements();
				int splitIndex = pairs.length / 2 + 1;
				block.bulkLoad(pairs, 0, splitIndex);
				newBlock.bulkLoad(pairs, splitIndex, pairs.length - splitIndex);

				// 3. Find the target block for the new element and add it.
				if (BitSignatureUtil.COMPARATOR.compare(pair.getBitSignature(),
						newBlock.getStartKey()) < 0) {
					block.insertElement(pair);
				} else {
					newBlock.insertElement(pair);
				}
			}
		}
	}

	public void deleteElement(long[] key) {
		LinkedBlock block = getBlockFor(key);
		if (block != null) {
			block.deleteElement(key);
		}
	}

	public IndexPair[] getNearestNeighbours(long[] key, int beamRadius) {
		LinkedBlock block = getBlockFor(key);
		int fetchSmallerElements = beamRadius, fetchGreaterElements = beamRadius;
		LinkedBlock smallerBlock = null, greaterBlock = null;
		List<IndexPair> neighbours = new ArrayList<IndexPair>(2 * beamRadius);
		if (block == null) {
			// All blocks' start keys are greater than the given key. So, the
			// best we can do, is to fetch all small elements.
			greaterBlock = firstBlock;
		} else {
			// Fetch the elements, find the middle of the beam, add the elements
			// from this block, and tell how many elements to fetch from other
			// blocks.
			IndexPair[] blockElements = block.getElements();
			int beamCenter = 0;
			for (IndexPair pair : blockElements) {
				if (BitSignatureUtil.COMPARATOR.compare(key, pair.getBitSignature()) <= 0) {
					break;
				}
				beamCenter++;
			}
			int startIndex = Math.max(0, beamCenter - beamRadius);
			int endIndex = Math.min(blockElements.length, beamCenter + beamRadius);
			for (int i = startIndex; i < endIndex; i++) {
				neighbours.add(blockElements[i]);
			}
			fetchSmallerElements -= (beamCenter - startIndex);
			smallerBlock = block.getPreviousBlock();
			
			fetchGreaterElements -= (endIndex - beamCenter);
			greaterBlock = block.getNextBlock();
		}
		
		// Fetch necessary elements from surrounding blocks.
		while (smallerBlock != null && fetchSmallerElements > 0) {
			int startIndex = Math.max(smallerBlock.getSize() - fetchSmallerElements, 0);
			int numElements = smallerBlock.getSize() - startIndex;
			IndexPair[] blockElements = smallerBlock.getElements(startIndex, numElements);
			for (IndexPair pair : blockElements) {
				neighbours.add(pair);
			}
			fetchSmallerElements -= numElements;
			smallerBlock = smallerBlock.getPreviousBlock();
		}
		while (greaterBlock != null && fetchGreaterElements > 0) {
			int numElements = Math.min(greaterBlock.getSize(), fetchGreaterElements);
			IndexPair[] blockElements = greaterBlock.getElements(0, numElements);
			for (IndexPair pair : blockElements) {
				neighbours.add(pair);
			}
			fetchGreaterElements -= numElements;
			greaterBlock = greaterBlock.getNextBlock();
		}
		
		return neighbours.toArray(new IndexPair[neighbours.size()]);
	}

	public void bulkLoad(IndexPair[] keyValuePairs) {
		try {
			int pairsPerBlock = (int) Math
					.ceil(INITIAL_LOAD_FACTOR * blockSize);
			// ceil keyValuePairs.length / pairsPerBlock
			int numBlocks = (keyValuePairs.length + pairsPerBlock - 1)
					/ pairsPerBlock;
			LinkedBlock currentBlock = null;
			LinkedBlock lastBlock = null;
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
				currentBlock.bulkLoad(keyValuePairs, offset, pairsToWrite);
				remainingPairs -= pairsToWrite;
				offset += pairsToWrite;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private LinkedBlock createNewBlock() throws IOException {
		LinkedBlock currentBlock;
		Path blockPath = basePath.resolve("blockIndex" + blockIdCounter++);
		currentBlock = new CachingBlock(blockSize, keySize, blockPath);
		return currentBlock;
	}

	public int getElement(long[] key) {
		LinkedBlock block = getBlockFor(key);
		if (block == null)
			throw new IllegalArgumentException("Key not present in index.");
		return block.get(key);
	}

	/**
	 * Find the block that potentially might contain the entry associated with
	 * the given key.
	 */
	private LinkedBlock getBlockFor(long[] key) {
		// Scan the blocks until we find a block that has a greater start key
		// than the given key.
		// If that does not happen, we need to return the last block.
		LinkedBlock lastValidBlock = null;
		for (LinkedBlock curBlock = firstBlock; curBlock != null; curBlock = curBlock
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
		for (LinkedBlock curBlock = firstBlock; curBlock != null; curBlock = curBlock
				.getNextBlock()) {
			size += curBlock.getSize();
		}
		return size;
	}

	public Iterator<IndexPair> iterator() {
		return new IndexIterator(firstBlock);
	}

	private class IndexIterator implements Iterator<IndexPair> {

		private LinkedBlock curBlock;
		private int curIndex;
		private IndexPair[] curIndexPairs;
		private IndexPair next;

		public IndexIterator(LinkedBlock startBlock) {
			if (startBlock == null)
				return;
			this.curBlock = startBlock;
			this.curIndexPairs = startBlock.getElements();
			if (this.curIndexPairs.length == 0)
				move();
			else {
				this.curIndex = 0;
				this.next = this.curIndexPairs[0];
			}
		}

		private void move() {
			while (true) {
				if (++curIndex < curIndexPairs.length) {
					next = curIndexPairs[curIndex];
					return;
				} else {
					curBlock = curBlock.getNextBlock();
					if (curBlock == null) {
						next = null;
						return;
					}
					curIndexPairs = curBlock.getElements();
					curIndex = -1;
				}
			}
		}

		public boolean hasNext() {
			return next != null;
		}

		public IndexPair next() {
			IndexPair curNext = next;
			move();
			return curNext;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	public void close() throws IOException {
		for (LinkedBlock curBlock = firstBlock; curBlock != null; curBlock = curBlock.getNextBlock()) {
			curBlock.close();
		}
	}

	public void recover() {
		this.basePath = FileUtils.toPath(basePathString);
		
		for (LinkedBlock block = firstBlock; block != null; block = block.getNextBlock()) {
			block.recover();
		}
	}

}
