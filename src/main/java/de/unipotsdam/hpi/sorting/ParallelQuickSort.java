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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelQuickSort<T> implements SortAlgorithm<T> {

	private int directSortingSize;

	private Comparator<T> comparator;

	private ForkJoinPool forkJoinPool = new ForkJoinPool();

	public ParallelQuickSort(int directSortingSize) {
		this.directSortingSize = directSortingSize;
	}

	public void setComparator(Comparator<T> comparator) {
		this.comparator = comparator;
	}

	public void sort(T[] data) {
		forkJoinPool.invoke(new QuickSortStep<T>(data, comparator, 0,
				data.length, directSortingSize));
	}

	public void close() {
		forkJoinPool.shutdown();
	}

	private static class QuickSortStep<T> extends RecursiveAction {

		private static final long serialVersionUID = 3178415532045387892L;

		private int endIndex;
		private int startIndex;
		private Comparator<T> comparator;
		private T[] data;
		private int directSortingSize;

		public QuickSortStep(T[] data, Comparator<T> comparator,
				int startIndex, int endIndex, int directSortingSize) {
			this.data = data;
			this.comparator = comparator;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}

		@Override
		protected void compute() {
			int sortingSize = endIndex - startIndex;
			if (sortingSize <= directSortingSize) {
				sortDirectly();
			} else if (sortingSize == 2) {
				compareAndSwap(startIndex, endIndex - 1);
			} else if (sortingSize > 2) {
				int middleIndex = splitData(startIndex, endIndex);
				List<RecursiveAction> nextSteps = new ArrayList<RecursiveAction>(
						2);
				if (middleIndex - startIndex > 1)
					nextSteps.add(createNewStep(startIndex, middleIndex));
				if (endIndex - middleIndex > 2)
					nextSteps.add(createNewStep(middleIndex + 1, endIndex));
				invokeAll(nextSteps);
			}
		}

		private QuickSortStep<T> createNewStep(int newStartIndex,
				int newEndIndex) {
			return new QuickSortStep<T>(data, comparator, newStartIndex,
					newEndIndex, directSortingSize);
		}

		private void sortDirectly() {
			Arrays.sort(data, startIndex, endIndex, comparator);
		}

		/**
		 * Splits the data into a two halves with lower and greater elements
		 * than a pivot element that is in the middle of the two halves.<br>
		 * <i>Notice: <code>endIndex - startIndex >= 2</code></i>
		 * 
		 * @param startIndex
		 *            start index of partial sorting array
		 * @param endIndex
		 *            exclusive index of partial sorting array
		 * @return the index of the pivot element
		 */
		private int splitData(int startIndex, int endIndex) {
			// find pivot element with median-of-three heuristic
			int middleIndex = (startIndex + endIndex) >> 1;
			int lastButOneIndex = endIndex - 2;
			swap(middleIndex, lastButOneIndex);
			compareAndSwap(startIndex, lastButOneIndex);
			compareAndSwap(startIndex, endIndex - 1);
			compareAndSwap(endIndex - 1, lastButOneIndex);
			int pivotIndex = endIndex - 1;

			// We already no that the first element is lower or equal to the
			// pivot.
			int lowPointer = startIndex;

			// We also know that the element right before the pivot is greater
			// or equal to the pivot.
			int highPointer = lastButOneIndex;

			// Now exchange elements until the pointers meet
			while (true) {
				while (++lowPointer < highPointer)
					if (compare(lowPointer, pivotIndex) > 0)
						break;
				while (--highPointer > lowPointer) {
					if (compare(pivotIndex, highPointer) > 0)
						break;
				}
				if (lowPointer < highPointer)
					swap(lowPointer, highPointer);
				else
					break;
			}

			// find where the pointers have crossed and bias towards the
			// upper half since we need to swap with the pivot element
			int boarderIndex = (lowPointer + highPointer + 1) >> 1;
			swap(boarderIndex, pivotIndex);

			// Verification code.
			// T pivot = data[boarderIndex];
			// for (int i = startIndex; i < boarderIndex; i++) {
			// T elem = data[i];
			// if (comparator.compare(elem, pivot) > 0) {
			// System.err.println("First half error on sorting size "
			// + (endIndex - startIndex));
			// }
			// }
			// for (int i = boarderIndex + 1; i < endIndex; i++) {
			// T elem = data[i];
			// if (comparator.compare(elem, pivot) < 0) {
			// System.err.println("Second half error on sorting size "
			// + (endIndex - startIndex));
			// }
			// }

			return boarderIndex;
		}

		/**
		 * Swaps the according elements in <code>data</code> if the element at
		 * <code>index1</code> is greater than the element at
		 * <code>index2</code>.
		 */
		private void compareAndSwap(int index1, int index2) {
			if (compare(index1, index2) > 0) {
				swap(index1, index2);
			}
		}

		/**
		 * Compares the elements at <code>index1</code> and <code>index2</code>
		 * in <code>data</code>.
		 */
		private int compare(int index1, int index2) {
			return comparator.compare(data[index1], data[index2]);
		}

		/**
		 * Swaps the elements at <code>index1</code> and <code>index2</code> in
		 * <code>data</code>.
		 */
		private void swap(int index1, int index2) {
			T temp = data[index1];
			data[index1] = data[index2];
			data[index2] = temp;
		}
	}

}
