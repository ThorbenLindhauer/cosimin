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
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.unipotsdam.hpi.util.BitSignatureUtil;
import de.unipotsdam.hpi.util.EncodingUtils;

/**
 * Uses {@link ByteBuffer} for writing elements to disk. We could implement
 * another version with {@link MappedByteBuffer}.
 * 
 * @author Thorben
 * 
 */
public class MappedIoBlock extends AbstractLinkedBlock {

	private static final long serialVersionUID = 8704103734709675740L;

	private static final int INT_SIZE_IN_BYTES = 4;

	private static final Set<OpenOption> FILE_OPEN_OPTIONS = new HashSet<OpenOption>();;
	static {
		FILE_OPEN_OPTIONS.add(StandardOpenOption.READ);
		FILE_OPEN_OPTIONS.add(StandardOpenOption.WRITE);
		FILE_OPEN_OPTIONS.add(StandardOpenOption.CREATE);
		FILE_OPEN_OPTIONS.add(StandardOpenOption.SYNC);
	}

	transient private MappedByteBuffer byteBuffer;
	transient private FileChannel channel;
	transient private Path filePath;

	private byte[] keyBuffer;

	/**
	 * 
	 * @param capacity
	 *            How many elements may be in this block
	 * @param keySize
	 *            How large is a key long array
	 * @throws IOException
	 *             if the file cannot be accessed properly
	 */
	public MappedIoBlock(int capacity, int keySize, Path filePath) throws IOException {
		this.capacity = capacity;
		this.keySize = keySize;
		this.filePath = filePath;
		this.keyBuffer = new byte[keySize * (BitSignatureUtil.BASE_TYPE_SIZE >> 3)];

		int requiredByteSize = (keyBuffer.length + INT_SIZE_IN_BYTES)
				* capacity;
		channel = FileChannel.open(filePath, FILE_OPEN_OPTIONS);
		byteBuffer = channel.map(MapMode.READ_WRITE, 0, requiredByteSize);

	}

	public Path getFilePath() {
		return filePath;
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

		size = 0;
		startKey = pairs[offset].getBitSignature();
		byteBuffer.position(0);
		for (int i = offset; i < offset + length; i++) {
			IndexPair pair = pairs[i];
			putAll(byteBuffer, pair.getBitSignature());
			byteBuffer.putInt(pair.getElementId());
		}
		size += length;
	}

	private void putAll(MappedByteBuffer buffer, long[] longs) {
		for (long l : longs) {
			buffer.putLong(l);
		}
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
	public synchronized IndexPair[] getElements(int startIndex, int numElements) {
		int bufferCapacity = byteBuffer.capacity();
		int startPos = getBufferPos(startIndex);
		int endPos = getBufferPos(startIndex + numElements);
		if (endPos > bufferCapacity) {
			throw new RuntimeException(
					"Out of bounds: Cannot access the end index " + endPos
							+ " in this block. Capacity is " + bufferCapacity);
		}

		if (!byteBuffer.isLoaded()) {
			byteBuffer.load();
		}
		byteBuffer.position(startPos);
		IndexPair[] pairs = new IndexPair[numElements];
		for (int i = 0; i < numElements; i++) {
			long[] key = getKey(byteBuffer);
			int elementId = byteBuffer.getInt();
			IndexPair pair = new IndexPair(key, elementId);
			pairs[i] = pair;
		}

		return pairs;
	}

	private long[] getKey(MappedByteBuffer buffer) {
		long[] key = new long[keySize];
		return getKey(buffer, key);
	}

	private long[] getKey(MappedByteBuffer buffer, long[] key) {
		byteBuffer.get(keyBuffer);
		EncodingUtils.copy(keyBuffer, key);
		return key;
	}

	/**
	 * Retrieves the element associated with the given key.
	 * 
	 * @throws IllegalArgumentException
	 *             if key not present in this block
	 */
	public synchronized int get(long[] key) {
		if (!byteBuffer.isLoaded()) {
			byteBuffer.load();
		}
		byteBuffer.position(0);
		long[] curKey = new long[keySize];
		for (int i = 0; i < size; i++) {
			getKey(byteBuffer, curKey);
			int elementId = byteBuffer.getInt();
			int comparison = BitSignatureUtil.COMPARATOR.compare(key, curKey);
			if (comparison == 0) {
				return elementId;
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
		startKey = elements[0].getBitSignature();
		byteBuffer.position(0);
		for (IndexPair element : elements) {
			if (Arrays.equals(key, element.getBitSignature()))
				continue;
			putAll(byteBuffer, element.getBitSignature());
			byteBuffer.putInt(element.getElementId());
		}
		size--;
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

	private int getBufferPos(int pairIndex) {
		return pairIndex * (keyBuffer.length + INT_SIZE_IN_BYTES);
	}

	/**
	 * Closes the underlying file channel and makes this block unusable!
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		byteBuffer.force();
		channel.close();
	}
	
	public void recover() {
		throw new RuntimeException("Not implemented.");
	}
}
