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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import de.unipotsdam.hpi.sparse.DefaultSparseIntList;
import de.unipotsdam.hpi.sparse.SparseIntList;

/**
 * Trivial input vector for testing purposes.
 * 
 * @author Sebastian
 * 
 */
public class IntArrayInputVector implements InputVector {

	private int[] values;
	private int id;

	private static AtomicInteger idCounter = new AtomicInteger(0);

	public IntArrayInputVector(int[] values) {
		this(idCounter.getAndIncrement(), values);
	}

	public IntArrayInputVector(int id, int[] values) {
		this.id = id;
		this.values = values;
	}

	public int size() {
		return values.length;
	}
	
	public int[] toIntArray() {
		return values;
	}
	
	public SparseIntList toSparseIntList() {
		return new DefaultSparseIntList(values);
	}

	@Override
	public String toString() {
		return "IntArrayInputVector" + Arrays.toString(values);
	}

	public int getId() {
		return id;
	}

	/**
	 * Generates a random input vector as specified by the settings.
	 * 
	 * @param size
	 *            dimension of the input vector
	 * @param componentRange
	 *            components will be in between <i>[-range/2; range/2 - 1]<i>
	 */
	public static InputVector generateInputRandomVector(int size,
			int componentRange) {
		Random random = new Random();
		int[] values = new int[size];
		for (int i = 0; i < size; i++)
			values[i] = random.nextInt(componentRange) - componentRange / 2;
		return new IntArrayInputVector(values);
	}

	/**
	 * Generates a sparse random input vector as specified by the settings.
	 * 
	 * @param size
	 *            dimension of the input vector
	 * @param componentRange
	 *            components will be in between <i>[-range/2; range/2 - 1]<i>
	 * @param denseness
	 *            the average amount of components filled
	 */
	public static InputVector generateSparseInputRandomVector(int size,
			int componentRange, double denseness) {
		Random random = new Random();
		int[] values = new int[size];
		for (int i = 0; i < size; i++)
			if (random.nextDouble() <= denseness)
				values[i] = random.nextInt(componentRange) - componentRange / 2;
		return new IntArrayInputVector(values);
	}

	public static InputVector mutateVector(InputVector inputVector,
			int componentRange, double mutationProb) {
		int[] values = inputVector.toIntArray().clone();
		Random random = new Random();
		for (int i = 0; i < values.length; i++) {
			if (random.nextDouble() <= mutationProb) {
				values[i] = random.nextInt(componentRange) - componentRange / 2;
			}
		}
		return new IntArrayInputVector(values);
	}

	public static InputVector mutateSparseVector(InputVector inputVector,
			int componentRange, double mutationProb, double density) {
		IntArrayInputVector mutatedVector = (IntArrayInputVector) mutateVector(
				inputVector, componentRange, mutationProb);
		enforceDensity(density, mutatedVector);
		return mutatedVector;
	}

	private static void enforceDensity(double density,
			IntArrayInputVector mutatedVector) {
		// Find set positions.
		IntList filledPositions = new IntArrayList();
		int[] components = mutatedVector.values;
		for (int i = 0; i < components.length; i++) {
			if (components[i] == 0) {
				filledPositions.add(i);
			}
		}
		
		// If necessary, randomly unset components to approximate desired density.
		double neededComponentsRate = density * components.length / filledPositions.size(); 
		if (neededComponentsRate < 1) {
			Random random = new Random();
			IntIterator iterator = filledPositions.iterator();
			while (iterator.hasNext()) {
				int index = iterator.nextInt();
				if (random.nextDouble() > neededComponentsRate) {
					components[index] = 0;
				}
			}
		}
	}

  public byte[] asBytes() {
    throw new UnsupportedOperationException("not implemented");
  }

}
