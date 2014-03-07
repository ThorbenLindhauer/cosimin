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

import de.unipotsdam.hpi.sparse.SparseIntList;

/**
 * This interface describes an input vector. It must be convertible to a sparse int list representation.
 * 
 * @author Sebastian
 * 
 */
public interface InputVector {

	/**
	 * @return the number of components in this vector
	 */
	int size();
	
	/**
	 * @return the <code>int[]</code> representation of this vector.
	 */
	int[] toIntArray();

	/**
	 * @return the same content as {@link #toIntArray()} but as
	 *         {@link SparseIntList}
	 */
	SparseIntList toSparseIntList();

	/**
	 * @return a unique id of this input vector
	 */
	int getId();

}
