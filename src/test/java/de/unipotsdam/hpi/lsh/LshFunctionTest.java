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

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import de.unipotsdam.hpi.input.InputVector;
import de.unipotsdam.hpi.input.IntArrayInputVector;
import de.unipotsdam.hpi.util.BitSignatureUtil;

public class LshFunctionTest {

	@Test
	public void testLocalitySensitiveness() {
		// Test parameters.
		final int VECTOR_SIZE = 100000;
		final int VECTOR_COMPONENT_RANGE = 100;
		final int DERIVED_VECTORS = 100;
		final int SIGNATURE_SIZE = 1024;
		final double MODIFICATION_PROBABILITY = 0.5;
		final int MODIFICATION_RANGE = 2;
		final double MAX_AVG_SIGNATURE_DISTANCE = 8d;

		// Initialize.
		Random random = new Random();
		LshFunction lshFunction = LshFunction.createRandomLSH(SIGNATURE_SIZE,
				VECTOR_SIZE);

		// Generate a base vector.
		InputVector baseVector = IntArrayInputVector.generateInputRandomVector(
				VECTOR_SIZE, VECTOR_COMPONENT_RANGE);
		long[] baseVectorSignature = lshFunction.createSignature(baseVector);

		// Create slightly modified vectors.
		double hammingDistance = 0d;
		for (int i = 0; i < DERIVED_VECTORS; i++) {
			int[] modifiedValues = baseVector.toIntArray().clone();
			for (int j = 0; j < VECTOR_SIZE; j++) {
				if (random.nextDouble() <= MODIFICATION_PROBABILITY) {
					int delta = random.nextInt(MODIFICATION_RANGE);
					delta = random.nextBoolean() ? delta : -delta;
					modifiedValues[j] += delta;
				}
			}
			// Save the hamming distance of the modified vector.
			long[] modifiedVectorSignature = lshFunction
					.createSignature(new IntArrayInputVector(modifiedValues));
			int curHammingDistance = BitSignatureUtil.calculateHammingDistance(
					baseVectorSignature, modifiedVectorSignature);
			hammingDistance += curHammingDistance;
		}
		hammingDistance /= DERIVED_VECTORS;
		String msg = String.format(
				"Expected a hamming distance less than %.3f, but was %.3f",
				MAX_AVG_SIGNATURE_DISTANCE, hammingDistance);
		Assert.assertTrue(msg, hammingDistance < MAX_AVG_SIGNATURE_DISTANCE);
	}
	
	@Test
	public void testLocalitySensitivenessWithSparseVectors() {
		// Test parameters.
		final int VECTOR_SIZE = 100000;
		final int VECTOR_COMPONENT_RANGE = 100;
		final int DERIVED_VECTORS = 100;
		final int SIGNATURE_SIZE = 1024;
		final double MODIFICATION_PROBABILITY = 0.5;
		final int MODIFICATION_RANGE = 2;
		final double MAX_AVG_SIGNATURE_DISTANCE = 8d;
		final double VECTOR_DENSITY = 0.01;
		
		// Initialize.
		Random random = new Random();
		LshFunction lshFunction = LshFunction.createRandomLSH(SIGNATURE_SIZE,
				VECTOR_SIZE);
		int expectedSetComponents = (int) Math.round(VECTOR_DENSITY * VECTOR_SIZE);
		
		// Generate a base vector.
		InputVector baseVector = IntArrayInputVector.generateSparseInputRandomVector(
				VECTOR_SIZE, VECTOR_COMPONENT_RANGE, VECTOR_DENSITY);
		long[] baseVectorSignature = lshFunction.createSignature(baseVector);
		
		// Create slightly modified vectors.
		double hammingDistance = 0d;
		for (int i = 0; i < DERIVED_VECTORS; i++) {
			int setComponents = 0;
			int[] modifiedValues = baseVector.toIntArray().clone();
			for (int j = 0; j < VECTOR_SIZE; j++) {
				if (modifiedValues[j] != 0 && random.nextDouble() <= MODIFICATION_PROBABILITY) {
					int delta = random.nextInt(MODIFICATION_RANGE);
					delta = random.nextBoolean() ? delta : -delta;
					modifiedValues[j] += delta;
				}
				if (modifiedValues[j] != 0) {
					setComponents++;
				}
			}
			if (setComponents < expectedSetComponents) {
				int unsetComponents = VECTOR_SIZE - setComponents;
				int componentsToSet = expectedSetComponents - setComponents;
				double setProbability = (double) componentsToSet / (double) unsetComponents;
				for (int j = 0; j < VECTOR_SIZE; j++) {
					if (modifiedValues[j] == 0 && random.nextDouble() <= setProbability) {
						int delta = random.nextInt(MODIFICATION_RANGE);
						delta = random.nextBoolean() ? delta : -delta;
						modifiedValues[j] += delta;
					}
				}
			}
			
			// Save the hamming distance of the modified vector.
			IntArrayInputVector modifiedInputVector = new IntArrayInputVector(modifiedValues);
			long[] modifiedVectorSignature = lshFunction
					.createSignature(modifiedInputVector);
			int curHammingDistance = BitSignatureUtil.calculateHammingDistance(
					baseVectorSignature, modifiedVectorSignature);
			hammingDistance += curHammingDistance;
		}
		hammingDistance /= DERIVED_VECTORS;
		String msg = String.format(
				"Expected a hamming distance less than %.3f, but was %.3f",
				MAX_AVG_SIGNATURE_DISTANCE, hammingDistance);
		Assert.assertTrue(msg, hammingDistance < MAX_AVG_SIGNATURE_DISTANCE);
	}
}
