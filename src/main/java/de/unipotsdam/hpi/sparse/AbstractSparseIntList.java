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

abstract public class AbstractSparseIntList implements SparseIntList {

	private static final long serialVersionUID = 4003864950859590544L;

	public abstract int size();

	public long scalarProduct(SparseIntList other) {
		SparseIntList.Cursor i1 = createCursor();
		SparseIntList.Cursor i2 = other.createCursor();

		long scalarProduct = 0;
		boolean moveI1, moveI2;

		boolean isI1Valid = i1.move();
		boolean isI2Valid = i2.move();
		while (isI1Valid && isI2Valid) {
			moveI1 = i1.getPosition() <= i2.getPosition();
			moveI2 = i2.getPosition() <= i1.getPosition();
			if (moveI1 && moveI2) {
				scalarProduct += i1.getValue() * i2.getValue();
			}
			if (moveI1) {
				isI1Valid = i1.move();
			}
			if (moveI2) {
				isI2Valid = i2.move();
			}
		}

		return scalarProduct;
	}

	public long scalarProduct(int[] other) {
		if (other.length != size()) {
			throw new IllegalArgumentException("Vector size " + other.length
					+ ", expected " + size());
		}

		long scalarProduct = 0;
		SparseIntList.Cursor cursor = createCursor();
		while (cursor.move()) {
			int position = cursor.getPosition();
			int value = cursor.getValue();
			int otherValue = other[position];
			scalarProduct += (long) otherValue * (long) value;
		}
		return scalarProduct;
	}

}
