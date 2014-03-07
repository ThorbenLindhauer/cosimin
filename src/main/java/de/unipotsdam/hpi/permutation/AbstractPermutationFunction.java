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

import de.unipotsdam.hpi.util.BitSignatureUtil;

public abstract class AbstractPermutationFunction implements
		PermutationFunction {

	private static final long serialVersionUID = 8518679840855246914L;

	private static final long MAX_BIT = 1L << (BitSignatureUtil.BASE_TYPE_SIZE - 1);
	
	/**
	 * 
	 * @param mapping
	 * @param input
	 * @param output has to be 0 initialized
	 */
	protected void createPermutation(int[] mapping, long[] input, long[] output) {
		int elementPos;
		int bitPos;

		for (int i = 0; i < mapping.length; i++) {
			
			// test bit i
			bitPos = i & BitSignatureUtil.BASE_TYPE_SIZE - 1;
			elementPos = i >> BitSignatureUtil.LOG_BASE_TYPE_SIZE;
			if ((input[elementPos] & (MAX_BIT >>> bitPos)) != 0) {
		
				// set bit in output
				int targetPosition = mapping[i];
				bitPos = targetPosition & (BitSignatureUtil.BASE_TYPE_SIZE - 1);
				elementPos = targetPosition >> BitSignatureUtil.LOG_BASE_TYPE_SIZE;
				output[elementPos] |= MAX_BIT >>> bitPos;
			}
		}
	}
	
}
