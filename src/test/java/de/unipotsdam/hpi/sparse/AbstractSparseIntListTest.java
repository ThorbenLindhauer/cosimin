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
package de.unipotsdam.hpi.sparse;

import org.junit.Assert;
import org.junit.Test;

public class AbstractSparseIntListTest {

	@Test
	public void testScalarProductWithSparseIntList() {
		int[] array1 = new int[] { 0, 1, 0, 3, 4, 0, 5, 0 };
		int[] array2 = new int[] { 2, 3, 0, 0, 0, 0, 5, 0 };
		SparseIntList list1 = new DefaultSparseIntList(array1, 0);
		SparseIntList list2 = new DefaultSparseIntList(array2, 0);
		
		int expectedScalarProduct = 3 + 25;
		
		Assert.assertEquals(expectedScalarProduct, list1.scalarProduct(list2));
		Assert.assertEquals(expectedScalarProduct, list2.scalarProduct(list1));
	}
	
	@Test
	public void testScalarProductWithIntArray() {
		int[] array1 = new int[] { 0, 1, 0, 3, 4, 0, 5, 0 };
		int[] array2 = new int[] { 2, 3, 0, 0, 0, 0, 5, 0 };
		SparseIntList list = new DefaultSparseIntList(array1, 0);
		
		int expectedScalarProduct = 3 + 25;
		
		Assert.assertEquals(expectedScalarProduct, list.scalarProduct(array2));
	}
}
