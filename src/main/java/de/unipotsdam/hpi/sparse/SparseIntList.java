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
