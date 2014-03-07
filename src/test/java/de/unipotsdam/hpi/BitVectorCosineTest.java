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
package de.unipotsdam.hpi;

import org.junit.Assert;
import org.junit.Test;

import de.unipotsdam.hpi.util.BitSignatureUtil;

public class BitVectorCosineTest {

  private double assertionTolerance = Double.MIN_VALUE;
  
	@Test
	public void testSelfCosine() {
		long[] signature = new long[]{ 1, 2, 64 };
		double cosine = BitSignatureUtil.calculateBitVectorCosine(signature, signature);
		Assert.assertEquals(1.0d, cosine, assertionTolerance);
	}
	
	@Test
	public void testMinimumCosine() {
		long[] signature1 = new long[]{ 1, 2, 64 }; 	// 00000001 00000010 01000000
		long[] signature2 = new long[]{ -2, -3, -65 };  // 11111110 11111101 10111111
		double cosine = BitSignatureUtil.calculateBitVectorCosine(signature1, signature2);
		Assert.assertEquals(-1.0d, cosine, assertionTolerance);
	}
	
	@Test
	public void testZeroCosine() {
		long[] signature1 = new long[]{ 0xFFFFFFFFL, 0xFFFFFFFFL, ~0xFFFFFFFFL }; 	// 00001111 00001111 11110000
		long[] signature2 = new long[]{ -1, -1, -1 };  // 11111111 11111111 11111111
		double cosine = BitSignatureUtil.calculateBitVectorCosine(signature1, signature2);
		
		// won't be exactly 0.0 because of double precision
		Assert.assertTrue(Math.abs(cosine) < 0.0001d);
	}
}
