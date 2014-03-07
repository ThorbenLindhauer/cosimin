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

import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.Random;

import de.unipotsdam.hpi.sparse.DefaultSparseIntList;
import de.unipotsdam.hpi.sparse.IntArraySparseIntList;
import de.unipotsdam.hpi.sparse.SparseIntList;
import de.unipotsdam.hpi.util.RandomNumberSampler;

/**
 * This class represents a hyperplane of arbitrary dimensions, which always goes
 * through the origin of its coordinate system.
 * 
 * @author Sebastian
 * 
 */
public class SparseHyperplane extends Hyperplane {

	private static final long serialVersionUID = -6347547553473283351L;

	private static final int SPARSE_PERCENTAGE = 99;

	private SparseIntList normalVector;

	/** This constructor serves test purposes. */
	public SparseHyperplane(int[] normalVector) {
		this(new IntArraySparseIntList(normalVector.clone()));
	}

	private SparseHyperplane(SparseIntList normalVector) {
		this.normalVector = normalVector;
	}

	/**
	 * Create a random hyperplane of the given size using the given sampler.
	 */
	public static SparseHyperplane createRandom(int size,
			RandomNumberSampler sampler) {
		Random random = new Random();
		int estimatedSize = size * (100 - SPARSE_PERCENTAGE + 5) / 100;
		DefaultSparseIntList normalVector = new DefaultSparseIntList(
				estimatedSize);

		for (int i = 0; i < size; i++) {
			if (random.nextInt(100) < SPARSE_PERCENTAGE)
				continue;
			double sampledValue = sampler.sample();
			if (sampledValue > MAX_SAMPLE)
				sampledValue = MAX_SAMPLE;
			else if (sampledValue < -MAX_SAMPLE)
				sampledValue = -MAX_SAMPLE;
			normalVector.add(i, (int) (SCALE_FACTOR * sampledValue));
		}

		return new SparseHyperplane(normalVector);
	}

	@Override
	public void adapt(IntList featureLengths, DoubleList weights,
			DoubleList featureMeans) {
		// TODO Establish or delete this class.
		throw new UnsupportedOperationException("Not implemented yet.");
	}
	
	public long scalarProduct(int[] vector) {
		// assert vector.length == normalVector.length;
		return normalVector.scalarProduct(vector);
	}

	@Override
	public long scalarProduct(SparseIntList vector) {
		return normalVector.scalarProduct(vector);
	}

	@Override
	public int getVectorSize() {
		return normalVector.size();
	}

}
