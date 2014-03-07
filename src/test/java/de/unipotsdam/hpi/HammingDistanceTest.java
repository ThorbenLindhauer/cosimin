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

public class HammingDistanceTest {

	@Test
	public void testSelfDistance() {
		long[] signature = new long[]{ 56, 23, 19 };
		int distance = BitSignatureUtil.calculateHammingDistance(signature, signature);
		Assert.assertEquals(0, distance);
	}
	
	@Test
	public void testHammingDistance() {
		long[] signature1 = new long[]{ 4, 20, 56 };	// 00000100 00010100 00111000
		long[] signature2 = new long[]{ 52, -60, 56 };  // 00110100 11000100 00111000

		int distance = BitSignatureUtil.calculateHammingDistance(signature1, signature2);
		Assert.assertEquals(61, distance);
	}
	
	@Test
	public void testUnequalVectorLength() {
		long[] signature1 = new long[] { 1, 2, 3 };
		long[] signature2 = new long[] { 1, 2 };
		
		try {
			BitSignatureUtil.calculateHammingDistance(signature1, signature2);
			Assert.fail("Exception expected");
		} catch (RuntimeException e) {
			// happy path
		}
	}
}
