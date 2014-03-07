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
package de.unipotsdam.hpi.permutation;

import java.util.Random;

@Deprecated
public class ApproximatePermutationFunction implements PermutationFunction {

	private static final long serialVersionUID = -8527728673857702553L;

	public static final int PERMUTATION_PRIME = 3001; 
	
	private int a, b, p;
	private int[] output;

	public ApproximatePermutationFunction(int numElements) {
		// TODO Generate p based on numElements
		p = PERMUTATION_PRIME;

		Random random = new Random();
		a = random.nextInt(p - 1) + 1;
		b = random.nextInt(p);
		
		output = new int[numElements];
		for (int i = 0; i < numElements; i++) {
			output[i] = (a * i + b) % p;
		}
	}
	
	public long[] permute(long[] signature) {
		throw new RuntimeException("Not implemented!");
	}

}
