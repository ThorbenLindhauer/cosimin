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

/**
 * SortAlgorithm that simply wraps {@link Arrays#sort(Object[], Comparator)}.
 * @author Sebastian
 *
 * @param <T> The type of element to be sorted.
 */
public class StandardLibSort<T> implements SortAlgorithm<T> {

	private Comparator<T> comparator;

	public void setComparator(Comparator<T> comparator) {
		this.comparator = comparator;
	}

	public void sort(T[] data) {
		if (comparator == null) {
			throw new IllegalStateException("No comparator was set!");
		}
		Arrays.sort(data, comparator);
	}
	
	public void close() {
	}

}
