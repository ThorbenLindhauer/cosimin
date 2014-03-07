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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class LimitingIteratorTest {

	@Test
	public void testLimitation() {
		List<Integer> baseList = Arrays.asList(1, 2, 3, 4, 5, 6);
		List<Integer> shortList = Arrays.asList(1, 2, 3, 4);
		Iterator<Integer> limitingIterator = new LimitingIterator<Integer>(
				baseList.iterator(), 4);

		for (Integer i : shortList) {
			Assert.assertTrue(limitingIterator.hasNext());
			Assert.assertEquals(i, limitingIterator.next());
		}
		Assert.assertFalse(limitingIterator.hasNext());
	}
	
	@Test
	public void testContinuousWrapping() {
		List<Integer> baseList = Arrays.asList(1, 2, 3, 4, 5, 6);
		Iterator<Integer> baseIterator = baseList.iterator();

		List<Integer> shortList1 = Arrays.asList(1, 2, 3, 4);
		List<Integer> shortList2 = Arrays.asList(5, 6);
		@SuppressWarnings("unchecked")
		List<List<Integer>> shortLists = Arrays.<List<Integer>>asList(shortList1, shortList2);
		
		for (List<Integer> shortList : shortLists) {
			Iterator<Integer> limitingIterator = new LimitingIterator<Integer>(
					baseIterator, 4);
			
			for (Integer i : shortList) {
				Assert.assertTrue(limitingIterator.hasNext());
				Assert.assertEquals(i, limitingIterator.next());
			}
			Assert.assertFalse(limitingIterator.hasNext());
		}
		
	}
	
	@Test
	public void testPrematureRunOut() {
		List<Integer> baseList = Arrays.asList(1, 2, 3, 4, 5, 6);
		Iterator<Integer> limitingIterator = new LimitingIterator<Integer>(
				baseList.iterator(), 8);

		for (Integer i : baseList) {
			Assert.assertTrue(limitingIterator.hasNext());
			Assert.assertEquals(i, limitingIterator.next());
		}
		Assert.assertFalse(limitingIterator.hasNext());
	}
	
	@Test
	public void testLimitingEmptyIterator() {
		Iterator<Integer> limitingIterator = new LimitingIterator<Integer>(
				Collections.<Integer>emptyIterator(), 8);
		Assert.assertFalse(limitingIterator.hasNext());
	}

}
