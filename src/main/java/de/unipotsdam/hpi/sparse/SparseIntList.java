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

import java.io.Serializable;

/**
 * This interface defines a sparse integer list. Lists that mostly contain
 * <code>0</code> values, can be stored more efficiently and especially allow
 * for a more efficient calculation of the scalar product.
 * 
 * @author Sebastian
 * 
 */
public interface SparseIntList extends Serializable {

	/** Calculates the scalar product with another {@link SparseIntList}. */
	long scalarProduct(SparseIntList other);

	/** Calculates the scalar product with another <code>int[]</code>. */
	long scalarProduct(int[] other);

	/** Creates a cursor for this list. */
	SparseIntList.Cursor createCursor();
	
	/**
	 * The number of components of the represented list.
	 */
	int size();

	/**
	 * The cursor is used to iterate through a {@link SparseIntList}.
	 * The values shall be iterated according to the positions in ascending. 
	 * @author Sebastian
	 *
	 */
	interface Cursor {
		
		/**
		 * Tries to move the cursor forward. Has to be called initially.
		 * @return whether the cursor could be moved
		 */
		boolean move();

		/**
		 * Returns the current value's position.
		 */
		int getPosition();

		/**
		 * Returns the current value.
		 */
		int getValue();
	}

}
