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
package de.unipotsdam.hpi.util;

import java.util.Comparator;

import org.junit.Assert;
import org.junit.Test;


public class ComparatorTest {

	@Test
	public void testSignatureComparison() {
		long[] signature1 = new long[]{ 1, 2, 64 }; 	// 00000001 00000010 01000000
		long[] signature2 = new long[]{ -2, -3, -65 };  // 11111110 11111101 10111111
		
		Comparator<long[]> comparator = BitSignatureUtil.COMPARATOR;
		int comparison = comparator.compare(signature1, signature1);
		Assert.assertEquals(0, comparison);
		
		comparison = comparator.compare(signature1, signature2);
		Assert.assertTrue(comparison < 0);
		
		comparison = comparator.compare(signature2, signature1);
		Assert.assertTrue(comparison > 0);
	}
	
	@Test
	public void testSameSignSignatureComparison() {
		long[] signature1 = new long[]{ 1, 2, 64 }; 	// 00000001 00000010 01000000
		long[] signature2 = new long[]{ 3, -3, -65 };   // 00000011 11111101 10111111
		
		Comparator<long[]> comparator = BitSignatureUtil.COMPARATOR;
		int comparison = comparator.compare(signature1, signature1);
		Assert.assertEquals(0, comparison);
		
		comparison = comparator.compare(signature1, signature2);
		Assert.assertTrue(comparison < 0);
		
		comparison = comparator.compare(signature2, signature1);
		Assert.assertTrue(comparison > 0);
	}
	
}
