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

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.Collections;

public class ListBasedPermutationFunction extends AbstractPermutationFunction {

	private static final long serialVersionUID = 2403060505967005297L;

	private int[] mapping;

	public ListBasedPermutationFunction(int numElements) {
		
		IntArrayList mappingList = new IntArrayList(numElements);
		for (int i = 0; i < numElements; i++) {
			mappingList.add(i);
		}
		Collections.shuffle(mappingList);
		mapping = mappingList.toIntArray();
	}
	
	public long[] permute(long[] signature) {
		long[] permutation = new long[signature.length];
		createPermutation(mapping, signature, permutation);
		return permutation;
	}
	
}
