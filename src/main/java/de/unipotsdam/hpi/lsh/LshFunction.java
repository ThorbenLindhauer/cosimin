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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.unipotsdam.hpi.input.InputVector;
import de.unipotsdam.hpi.sparse.SparseIntList;
import de.unipotsdam.hpi.util.BitSignatureUtil;
import de.unipotsdam.hpi.util.BoxMullerSampler;
import de.unipotsdam.hpi.util.RandomNumberSampler;

/**
 * Represents a LSH function containing several hyperplanes.
 * 
 * @author Sebastian
 * 
 */
public class LshFunction implements Serializable {

	private static final boolean USE_SPARSE_HYPERPLANES = false;

	private static final long serialVersionUID = 8892227243669482977L;

	private List<Hyperplane> hyperplanes;

	private LshFunction(int numHyperplanes) {
		hyperplanes = new ArrayList<Hyperplane>(numHyperplanes);
	}

	/**
	 * Calculates the hash value of the given vector.
	 */
	public long[] createSignature(InputVector inputVector) {
		int signatureLength = (hyperplanes.size()
				+ BitSignatureUtil.BASE_TYPE_SIZE - 1) >> BitSignatureUtil.LOG_BASE_TYPE_SIZE;

		long[] signature = new long[signatureLength];
		SparseIntList vector = inputVector.toSparseIntList();
		
		long bufferItem = 0;
		int bufferPos = BitSignatureUtil.BASE_TYPE_SIZE;
		int signaturePos = 0;
		for (Hyperplane hyperplane : hyperplanes) {
			bufferPos--;

			if (hyperplane.isOnPositiveSide(vector)) {
				bufferItem |= (0x01L << bufferPos);
			}

			if (bufferPos == 0) {
				signature[signaturePos] = bufferItem;
				signaturePos++;
				bufferPos = BitSignatureUtil.BASE_TYPE_SIZE;
				bufferItem = 0;
			}

		}

		if (signaturePos < signatureLength) {
			signature[signaturePos] = bufferItem;
		}

		return signature;
	}
	
	public void adaptWeights(IntList featureLengths, DoubleList weights, DoubleList featureMeans) {
		for (Hyperplane hyperplane : hyperplanes) {
			hyperplane.adapt(featureLengths, weights, featureMeans);
		}
	}

	@Override
	public String toString() {
		if (!hyperplanes.isEmpty())
			return "LSHFunction["+hyperplanes.size()+"x"+hyperplanes.get(0).getVectorSize()+"]";
		return super.toString();
	}

	/**
	 * Generates a random LSH function.
	 * 
	 * @param numHyperplanes
	 *            number of bits, that the hash values will have
	 * @param inputVectorSize
	 *            dimension of vectors to be hashed
	 */
	public static LshFunction createRandomLSH(int numHyperplanes,
			int inputVectorSize) {
		if (numHyperplanes % BitSignatureUtil.BASE_TYPE_SIZE != 0)
			System.err
					.println("Signature size is not filling the base data type ("
							+ numHyperplanes + ")!");
		LshFunction lshFunction = new LshFunction(numHyperplanes);
		RandomNumberSampler sampler = new BoxMullerSampler();
		for (int i = 0; i < numHyperplanes; i++) {
			Hyperplane hyperplane = USE_SPARSE_HYPERPLANES ? SparseHyperplane
					.createRandom(inputVectorSize, sampler) : DenseHyperplane
					.createRandom(inputVectorSize, sampler);
			lshFunction.hyperplanes.add(hyperplane);
		}
		return lshFunction;
	}

}
