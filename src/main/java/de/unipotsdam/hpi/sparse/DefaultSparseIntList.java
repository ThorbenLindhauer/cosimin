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
package de.unipotsdam.hpi.sparse;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.unipotsdam.hpi.util.EncodingUtils;

public class DefaultSparseIntList extends AbstractSparseIntList {

	private static final long serialVersionUID = -3127191810083646648L;

	private IntArrayList positions;
	private IntArrayList values;
	private int size;
	
	public DefaultSparseIntList(int expectedSize) {
		positions = new IntArrayList(expectedSize);
		values = new IntArrayList(expectedSize);
	}

	/**
	 * Creates a sparse representation of <code>array</code>.
	 */
	public DefaultSparseIntList(int[] array) {
		this(array, array.length / 2);
	}
	
	/**
	 * Creates a sparse representation of <code>array</code>, whereas there
	 * are <code>expectedSize</code> non-null values expected in array.
	 */
	public DefaultSparseIntList(int[] array, int expectedSize) {
		this(expectedSize);
		for (int i = 0; i < array.length; i++) {
			add(i, array[i]);
		}
		trim();
		size = array.length;
	}

	public void trim() {
		positions.trim();
		values.trim();
	}

	/**
	 * Adds a value to this sparse list.<br>
	 * Only call this function if <code>position</code> is greater than the
	 * last added value's position.
	 */
	public void add(int position, int value) {
		if (value != 0) {
			positions.add(position);
			values.add(value);
		}
		size = Math.max(size, position + 1);
	}

	public Cursor createCursor() {
		return new SparseIntList.Cursor() {

			private int index = -1;

			public boolean move() {
				return ++index < values.size();
			}

			public int getValue() {
				return values.getInt(index);
			}

			public int getPosition() {
				return positions.getInt(index);
			}
		};
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("DefaultSparseIntList[");
		String separator = "";
		Cursor cursor = createCursor();
		while (cursor.move()) {
			sb.append(separator).append(cursor.getValue()).append("@").append(cursor.getPosition());
			separator = ", ";
		}
		sb.append("]");
		return sb.toString();
	}
	
	public byte[] toBytes() {
		try {
			int outputSize = (2 + values.size() + positions.size()) * 4;
			trim();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(outputSize);
			EncodingUtils.writeInt(size, outputStream);
			EncodingUtils.writeInt(values.size(), outputStream);
			EncodingUtils.write(values.elements(), outputStream);
			EncodingUtils.write(positions.elements(), outputStream);
			outputStream.close();
			return outputStream.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Unexpected exception", e);
		}
	}
	
	public static DefaultSparseIntList fromBytes(byte[] bytes) {
		try {
			
			ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
			int size = EncodingUtils.readInt(inputStream);
			int capacity = EncodingUtils.readInt(inputStream);
			byte[] readBuffer = new byte[capacity * 4];
			int[] values = new int[capacity];
			int[] positions = new int[capacity];
			EncodingUtils.readCompleteArray(values, readBuffer, inputStream);
			EncodingUtils.readCompleteArray(positions, readBuffer, inputStream);
			inputStream.close();
			
			DefaultSparseIntList sparseIntList = new DefaultSparseIntList(capacity);
			for (int i = 0; i < capacity; i++) {
				sparseIntList.add(positions[i], values[i]);
			}
			sparseIntList.setSize(size);
			
			return sparseIntList;
		} catch (IOException e) {
			throw new RuntimeException("Unexpected exception", e);
		}
	}

	public int size() {
		return size;
	}
	
	public void setSize(int size) {
		// TODO at this point, we could do sanitiy checks
		this.size = size;
	}
}
