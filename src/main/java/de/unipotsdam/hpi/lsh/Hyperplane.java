package de.unipotsdam.hpi.lsh;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.io.Serializable;

import de.unipotsdam.hpi.sparse.SparseIntList;

/**
 * This class represents a hyperplane of arbitrary dimensions, which always goes
 * through the origin of its coordinate system.
 * 
 * @author Sebastian
 * 
 */
public abstract class Hyperplane implements Serializable {

	private static final long serialVersionUID = -6347547553473283351L;

	/** Sampled values get multiplied by this factor. */
	protected static final int SCALE_FACTOR = 100;

	/**
	 * Maximum value to sample from the standard Gaussian.<br>
	 * P(-3 <= sample <= 3) = 0.998, so this value is pretty fair.
	 */
	protected static final double MAX_SAMPLE = 3d;

	/**
	 * Calculate the scalar product of this hyperplane's normal vector with the
	 * given values.
	 * 
	 * @param vector
	 *            an array representing the vector to multiply with (must be of
	 *            same size as this hyperplane's normal vector)
	 * @return
	 */
	public abstract long scalarProduct(int[] vector);

	/**
	 * Calculate the scalar product of this hyperplane's normal vector with the
	 * given values.
	 * 
	 * @param vector
	 *            a sparse int list representing the vector to multiply with
	 * @return
	 */
	public abstract long scalarProduct(SparseIntList vector);

	/**
	 * Checks on which side of the hyperplane a given point (represented by a
	 * vector) lies.
	 */
	public boolean isOnPositiveSide(int[] vector) {
		return scalarProduct(vector) > 0;
	}

	public boolean isOnPositiveSide(SparseIntList vector) {
		return scalarProduct(vector) > 0;
	}

	public abstract int getVectorSize();

	public abstract void adapt(IntList featureLengths, DoubleList weights, DoubleList featureMeans);

}
