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
