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
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import de.unipotsdam.hpi.input.InputVector;
import de.unipotsdam.hpi.input.IntArrayInputVector;
import de.unipotsdam.hpi.util.BitSignatureUtil;

public class CosineSimilarityApproximationTest {

  private static final Logger logger = Logger.getLogger(CosineSimilarityApproximationTest.class.getName());
  
	@Test
	public void testCosineSimilarityApproximation() {
		// Test parameters.
		final int VECTOR_SIZE = 100000;
		final int VECTOR_COMPONENT_RANGE = 100;
		final int VECTOR_PAIRS = 100;
		final int SIGNATURE_SIZE = 1024;
		final double MAX_RMSE = 0.05d;

		// Generate an LSH function.
		LshFunction lshFunction = LshFunction.createRandomLSH(SIGNATURE_SIZE,
				VECTOR_SIZE);

		// Generate pairs and compare the original and estimated cosine
		// similarity.
		double rmse = 0d;
		InputVector vector1 = IntArrayInputVector.generateInputRandomVector(VECTOR_SIZE,
				VECTOR_COMPONENT_RANGE);
		long[] signature1 = lshFunction.createSignature(vector1);
		Random random = new Random();
		for (int i = 0; i < VECTOR_PAIRS; i++) {
			InputVector vector2 = IntArrayInputVector.mutateVector(vector1, VECTOR_COMPONENT_RANGE, random.nextDouble());

			double actualCosSim = cosineSimilarity(vector1, vector2);
			long[] signature2 = lshFunction.createSignature(vector2);
			double estimatedCosSim = BitSignatureUtil.calculateBitVectorCosine(
					signature1, signature2);
			
			logger.info(actualCosSim+ " - "+estimatedCosSim);
			double delta = actualCosSim - estimatedCosSim;
			rmse += delta * delta;
			
			vector1 = vector2;
			signature1 = signature2;
		}
		rmse /= VECTOR_PAIRS;
		rmse = Math.sqrt(rmse);
		String message = "Actual RMSE: " + rmse + ", should be <= " + MAX_RMSE;
		Assert.assertTrue(message, rmse <= MAX_RMSE);
	}
	
	@Test
	public void testCosineSimilarityApproximationWithSparseVectors() {
//		Assert.fail("Pending.");
		
		// Test parameters.
		final int VECTOR_SIZE = 100000;
		final int VECTOR_COMPONENT_RANGE = 100;
		final int VECTOR_PAIRS = 100;
		final int SIGNATURE_SIZE = 1024;
		final double MAX_RMSE = 0.05d;
		final double DENSITY = 0.01;
		
		// Generate an LSH function.
		LshFunction lshFunction = LshFunction.createRandomLSH(SIGNATURE_SIZE,
				VECTOR_SIZE);
		
		// Generate pairs and compare the original and estimated cosine
		// similarity.
		double rmse = 0d;
		InputVector vector1 = IntArrayInputVector.generateSparseInputRandomVector(VECTOR_SIZE,
				VECTOR_COMPONENT_RANGE, DENSITY);
		long[] signature1 = lshFunction.createSignature(vector1);
		Random random = new Random();
		for (int i = 0; i < VECTOR_PAIRS; i++) {
			InputVector vector2 = IntArrayInputVector.mutateSparseVector(vector1, VECTOR_COMPONENT_RANGE, random.nextDouble(), DENSITY);
			
			double actualCosSim = cosineSimilarity(vector1, vector2);
			long[] signature2 = lshFunction.createSignature(vector2);
			double estimatedCosSim = BitSignatureUtil.calculateBitVectorCosine(
					signature1, signature2);
			
			double delta = actualCosSim - estimatedCosSim;
			rmse += delta * delta;
			
			vector1 = vector2;
			signature1 = signature2;
		}
		rmse /= VECTOR_PAIRS;
		rmse = Math.sqrt(rmse);
		String message = "Actual RMSE: " + rmse + ", should be <= " + MAX_RMSE;
		Assert.assertTrue(message, rmse <= MAX_RMSE);
	}

	private double cosineSimilarity(InputVector v1, InputVector v2) {
		int scalarProduct = scalarProduct(v1, v2);
		double length1 = calculateLength(v1);
		double length2 = calculateLength(v2);
		return scalarProduct / (length1 * length2);
	}

	private double calculateLength(InputVector vector) {
		return Math.sqrt(scalarProduct(vector, vector));
	}

	private int scalarProduct(InputVector v1, InputVector v2) {
		int product = 0;
		int[] val1 = v1.toIntArray(); 
		int[] val2 = v2.toIntArray(); 
		for (int i = 0; i < val1.length; i++) {
			product += val1[i] * val2[i];
		}
		return product;
	}
}
