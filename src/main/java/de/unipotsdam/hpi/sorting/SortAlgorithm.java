package de.unipotsdam.hpi.sorting;

import java.util.Comparator;

public interface SortAlgorithm<T> {

	void setComparator(Comparator<T> comparator);
	
	void sort(T[] data);
	
	void close();
	
}
