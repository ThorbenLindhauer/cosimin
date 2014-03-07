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
package de.unipotsdam.hpi.lsh;

import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import de.unipotsdam.hpi.sparse.SparseIntList;
import de.unipotsdam.hpi.util.RandomNumberSampler;

/**
 * Non-sparse hyperplane implementation.
 * 
 * @author Sebastian
 * 
 */
public class DenseHyperplane extends Hyperplane {

	private static final long serialVersionUID = -6347547553473283351L;

	private int[] normalVector;

	public DenseHyperplane(int[] normalVector) {
		this.normalVector = normalVector.clone();
	}

	/**
	 * Create a random hyperplane of the given size using the given sampler.
	 */
	public static DenseHyperplane createRandom(int size,
			RandomNumberSampler sampler) {
		int[] normalVector = new int[size];
		for (int i = 0; i < size; i++) {
			double sampledValue = sampler.sample();
			if (sampledValue > MAX_SAMPLE)
				sampledValue = MAX_SAMPLE;
			else if (sampledValue < -MAX_SAMPLE)
				sampledValue = -MAX_SAMPLE;
			normalVector[i] = (int) (SCALE_FACTOR * sampledValue);
		}

		return new DenseHyperplane(normalVector);
	}

	@Override
	public void adapt(IntList featureLengths, DoubleList weights,
			DoubleList featureMeans) {
		IntListIterator featureLenghtsIterator = featureLengths.iterator();
		DoubleListIterator weightsIterator = weights.iterator();
		DoubleIterator meanVectorIterator = featureMeans.iterator();

		int curPos = 0;
		while (featureLenghtsIterator.hasNext()) {
			int length = featureLenghtsIterator.nextInt();
			double weight = weightsIterator.nextDouble();
			double featureMean = meanVectorIterator.nextDouble();

			for (int i = 0; i < length; i++) {
				normalVector[curPos] = (int) Math.rint(weight
						* normalVector[curPos] / featureMean);
				curPos++;
			}
		}
	}

	/**
	 * Calculate the scalar product of this hyperplane's normal vector with the
	 * given values.
	 * 
	 * @param vector
	 *            an array representing the vector to multiply with (must be of
	 *            same size as this hyperplane's normal vector)
	 * @return
	 */
	public long scalarProduct(int[] vector) {
		if (vector.length != normalVector.length) {
			throw new RuntimeException("Illegal vector length: "
					+ vector.length + " (expected " + normalVector.length + ")");
		}

		long sum = 0;
		for (int i = 0; i < vector.length; i++) {
			sum += vector[i] * normalVector[i];
		}
		return sum;
	}

	@Override
	public long scalarProduct(SparseIntList vector) {
		return vector.scalarProduct(this.normalVector);
	}

	@Override
	public int getVectorSize() {
		return normalVector.length;
	}

}
