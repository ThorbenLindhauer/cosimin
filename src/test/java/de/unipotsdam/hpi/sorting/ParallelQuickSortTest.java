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
package de.unipotsdam.hpi.sorting;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import de.unipotsdam.hpi.util.Profiler;

public class ParallelQuickSortTest {

  private static final Logger logger = Logger.getLogger(ParallelQuickSortTest.class.getName());
  
	private static final Comparator<Integer> INTEGER_COMPARATOR = new Comparator<Integer>() {

		public int compare(Integer i1, Integer i2) {
			return i1.compareTo(i2);
		}

	};

	@Test
	public void testParallelQuickSortOnEmptyList() {
		runTest(new Integer[0]);
	}
	
	@Test
	public void testParallelQuickSortOnSingletonList() {
		runTest(new Integer[] { 1 });
	}
	
	@Test
	public void testParallelQuickSortOnReverseTwoItemList() {
		runTest(new Integer[] { 2, 1 });
	}

	@Test
	public void testParallelQuickSortOnTwoItemList() {
		runTest(new Integer[] { 1, 2 });
	}
	
	@Test
	public void testOnLargeData() {
		runTest(generateData(10000000));
	}

	private Integer[] generateData(int size) {
		Integer[] data = new Integer[size];
		Random random = new Random();
		for (int i = 0; i < size; i++) {
			data[i] = random.nextInt(size);
		}
		return data;
	}
	
	private void runTest(Integer[] originalList) {
		ParallelQuickSort<Integer> parallelQuickSort = new ParallelQuickSort<Integer>(
				100000);
		Profiler.clear();
		logger.info("Sorting parallely");
		Integer[] listToSort = originalList.clone();
		parallelQuickSort.setComparator(INTEGER_COMPARATOR);
		Profiler.start("parallel");
		parallelQuickSort.sort(listToSort);
		Profiler.stop("parallel");

		logger.info("Sorting sequentially");
		Profiler.start("sequential");
		Arrays.sort(originalList, INTEGER_COMPARATOR);
		Profiler.stop("sequential");
		
		Assert.assertArrayEquals(originalList, listToSort);
		logger.info("Sorted "+originalList.length+" elements");
		Profiler.printMeasurements();
	}

}
