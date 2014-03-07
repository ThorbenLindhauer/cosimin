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
