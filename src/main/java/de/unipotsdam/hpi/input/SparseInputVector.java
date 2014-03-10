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
package de.unipotsdam.hpi.input;

import de.unipotsdam.hpi.sparse.DefaultSparseIntList;
import de.unipotsdam.hpi.sparse.SparseIntList;

public class SparseInputVector implements InputVector {

	private DefaultSparseIntList sparseIntList;
	private int id;
	
	public SparseInputVector(InputVector vector) {
		this(vector.getId(), (DefaultSparseIntList) vector.toSparseIntList());
	}

	public SparseInputVector(int id, DefaultSparseIntList sparseIntList) {
		this.id = id;
		this.sparseIntList = sparseIntList;
	}

	
	public int size() {
		return sparseIntList.size();
	}

	public int[] toIntArray() {
		throw new UnsupportedOperationException("Not implemented.");
	}

	public SparseIntList toSparseIntList() {
		return sparseIntList;
	}

	public int getId() {
		return id;
	}

	public static SparseInputVector fromBytes(int id, byte[] bytes) {
		return new SparseInputVector(id, DefaultSparseIntList.fromBytes(bytes));
	}
	
	public byte[] valuesToBytes() {
	  return sparseIntList.toBytes();
	}
	

}
