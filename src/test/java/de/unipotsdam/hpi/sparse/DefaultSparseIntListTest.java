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

import de.unipotsdam.hpi.sparse.SparseIntList.Cursor;

public class DefaultSparseIntListTest {

	@Test
	public void testAdding() {
		int[] positions = { 2, 4, 6, 9 };
		int[] values = { 2, 3, 5, 7 };

		DefaultSparseIntList sparseIntList = new DefaultSparseIntList(4);
		for (int i = 0; i < positions.length; i++) {
			sparseIntList.add(positions[i], values[i]);
		}

		SparseIntList.Cursor cursor = sparseIntList.createCursor();
		for (int i = 0; i < positions.length; i++) {
			Assert.assertTrue(cursor.move());
			Assert.assertEquals(positions[i], cursor.getPosition());
			Assert.assertEquals(values[i], cursor.getValue());
		}
		Assert.assertFalse(cursor.move());
		Assert.assertEquals(10, sparseIntList.size());
	}

	@Test
	public void testInitializingWithArray() {
		int[] array = { 0, 0, 2, 0, 3, 0, 5, 0, 0, 7, 0 };
		int[] positions = { 2, 4, 6, 9 };
		int[] values = { 2, 3, 5, 7 };

		DefaultSparseIntList sparseIntList = new DefaultSparseIntList(array, 4);

		SparseIntList.Cursor cursor = sparseIntList.createCursor();
		for (int i = 0; i < positions.length; i++) {
			Assert.assertTrue(cursor.move());
			Assert.assertEquals(positions[i], cursor.getPosition());
			Assert.assertEquals(values[i], cursor.getValue());
		}
		Assert.assertFalse(cursor.move());
		Assert.assertEquals(array.length, sparseIntList.size());
	}

	
	@Test
	public void testByteSerialization() {
		int[] array = { 0, 0, 2, 0, 3, 0, 5, 0, 0, 7, 0 };
		
		DefaultSparseIntList sparseIntList = new DefaultSparseIntList(array, 4);
		byte[] bytes = sparseIntList.toBytes();
		DefaultSparseIntList copy = DefaultSparseIntList.fromBytes(bytes);
		
		Cursor cursor1 = sparseIntList.createCursor();
		Cursor cursor2 = copy.createCursor();
		
		while (cursor1.move() && cursor2.move()) {
			Assert.assertEquals(cursor1.getPosition(), cursor2.getPosition());
			Assert.assertEquals(cursor1.getValue(), cursor2.getValue());
		}
		
		Assert.assertFalse(cursor1.move());
		Assert.assertFalse(cursor2.move());

		Assert.assertEquals(array.length, sparseIntList.size());
		Assert.assertEquals(array.length, copy.size());
	}
}
