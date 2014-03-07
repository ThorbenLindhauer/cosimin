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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import de.unipotsdam.hpi.util.BitSignatureUtil;
import de.unipotsdam.hpi.util.EncodingUtils;

/**
 * Is associated with a file that contains the actual elements of this block.<br>
 * It also uses a {@link SoftReference} to keep elements in memory as long as possible.
 * 
 * @author Sebastian
 */
public class CachingBlock extends AbstractLinkedBlock implements Serializable {
	
	private static final long serialVersionUID = 8965038039252466052L;
	
	private static final Logger logger = Logger.getLogger(CachingBlock.class.getName());

	private static AtomicInteger hitCount = new AtomicInteger();
	private static AtomicInteger missCount = new AtomicInteger();

	static final int INT_SIZE_IN_BYTES = 4;

	transient private SoftReference<IndexPair[]> cache;

	private File file;

	protected byte[] keyBuffer;

	/**
	 * 
	 * @param capacity
	 *            How many elements may be in this block
	 * @param keySize
	 *            How large is a key long array
	 * @throws IOException
	 *             if the file cannot be accessed properly
	 */
	public CachingBlock(int capacity, int keySize, Path filePath)
			throws IOException {
		this.capacity = capacity;
		this.keySize = keySize;
		this.file = filePath.toFile();
		this.keyBuffer = new byte[keySize
				* (BitSignatureUtil.BASE_TYPE_SIZE >> 3)];
	}

	/**
	 * Inserts all pairs into the block.
	 * 
	 * @see #bulkLoad(IndexPair[], int, int)
	 */
	public void bulkLoad(IndexPair[] pairs) {
		bulkLoad(pairs, 0, pairs.length);
	}

	/**
	 * Inserts a subset of pairs of the given array. The pairs must fit into
	 * this block. If there are less pairs than the block's capacity, the rest
	 * remains unchanged. The pairs are assumed to be already sorted.
	 */
	public void bulkLoad(IndexPair[] pairs, int offset, int length) {
		if (length > capacity) {
			throw new RuntimeException("Cannot write " + length
					+ " elements to a block of size " + capacity);
		}
		if (length == 0) {
			return;
		}
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
			for (int i = offset; i < offset + length; i++) {
				IndexPair pair = pairs[i];
				writePair(out, pair);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		startKey = pairs[offset].getBitSignature();
		size = length;
		IndexPair[] cachedPairs = new IndexPair[length];
		System.arraycopy(pairs, offset, cachedPairs, 0, length);
		cache = new SoftReference<IndexPair[]>(cachedPairs);
	}

	private void writePair(OutputStream out, IndexPair pair)
			throws IOException {
		EncodingUtils.writeLongArray(pair.getBitSignature(), out);
		EncodingUtils.writeInt(pair.getElementId(), out);
	}

	/** Returns all elements of this block. */
	public IndexPair[] getElements() {
		return getElements(0, size);
	}

	/**
	 * get a number of sequential elements of this block starting from a
	 * specified index
	 * 
	 * @param startIndex
	 * @param numElements
	 */
	public IndexPair[] getElements(int startIndex, int numElements) {

		if (startIndex + numElements > size) {
			throw new RuntimeException(
					"Out of bounds: Cannot access the end index "
							+ (startIndex + numElements)
							+ " in this block. Size is " + size);
		}

		IndexPair[] pairs = getOrLoadPairs();
		if (numElements == size)
			return pairs;

		IndexPair[] result = new IndexPair[numElements];
		System.arraycopy(pairs, startIndex, result, 0, numElements);
		return result;
	}

	/**
	 * get a number of sequential elements of this block starting from a
	 * specified index
	 * 
	 * @param startIndex
	 * @param numElements
	 */
	private synchronized IndexPair[] getOrLoadPairs() {
		IndexPair[] pairs;
		if (cache != null && (pairs = cache.get()) != null) {
			hitCount.incrementAndGet();
			return pairs;
		}

		missCount.incrementAndGet();
		pairs = new IndexPair[size];
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			// in.skip(startPos);
			for (int i = 0; i < size; i++) {
				IndexPair pair = readPair(in);
				pairs[i] = pair;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

		this.cache = new SoftReference<IndexPair[]>(pairs);

		return pairs;
	}

	private IndexPair readPair(FileInputStream in) throws IOException {
		long[] key = new long[keySize];
		EncodingUtils.readCompleteArray(key, keyBuffer, in);
		int elementId = EncodingUtils.readInt(in);
		IndexPair pair = new IndexPair(key, elementId);
		return pair;
	}

	/**
	 * Retrieves the element associated with the given key.
	 * 
	 * @throws IllegalArgumentException
	 *             if key not present in this block
	 */
	public int get(long[] key) {
		IndexPair[] pairs = getOrLoadPairs();
		for (IndexPair pair : pairs) {
			int comparison = BitSignatureUtil.COMPARATOR.compare(key,
					pair.getBitSignature());
			if (comparison == 0) {
				return pair.getElementId();
			} else if (comparison < 0) {
				break;
			}
		}
		throw new IllegalArgumentException("No such entry found in the block: "
				+ Arrays.toString(key));
	}

	/**
	 * Deletes the element with the specified key, if any.
	 */
	public void deleteElement(long[] key) {
		if (size == 0)
			return;

		IndexPair[] elements = getElements();
		List<IndexPair> retainedElements = new LinkedList<IndexPair>();
		for (IndexPair element : elements) {
			if (!Arrays.equals(element.getBitSignature(), key))
				retainedElements.add(element);
		}

		bulkLoad(retainedElements
				.toArray(new IndexPair[retainedElements.size()]));
	}

	/**
	 * Inserts the given pair into this block.
	 * 
	 * @throws IllegalStateException
	 *             if the block is already full
	 */
	public void insertElement(IndexPair indexPair) {
		if (size >= capacity)
			throw new IllegalStateException("Block is full!");
		
		IndexPair[] oldPairs = getElements();
		IndexPair[] newPairs = new IndexPair[oldPairs.length + 1];
		newPairs[0] = indexPair;
		System.arraycopy(oldPairs, 0, newPairs, 1, oldPairs.length);
		Arrays.sort(newPairs, IndexPair.COMPARATOR);

		bulkLoad(newPairs);
	}
	
	/**
	 * Closes the underlying file channel and makes this block unusable!
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
//		channel.close();
	}
	
	public void clearCache() {
		cache = null;
	}
	
	public static void printStatistics() {
		int hits = hitCount.get();
		int misses = missCount.get();
		
		double accesses = hits + misses;
		logger.info(String.format("Hit rate:  %3.3f (%4d)\n", hits / accesses, hits));
	}

	public void recover() {
	}
}
