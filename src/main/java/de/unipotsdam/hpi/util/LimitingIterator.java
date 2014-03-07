package de.unipotsdam.hpi.util;

import java.util.Iterator;

public class LimitingIterator<E> implements Iterator<E> {

	private Iterator<E> baseIterator;
	private int remainingElements;

	public LimitingIterator(Iterator<E> baseIterator, int limit) {
		this.baseIterator = baseIterator;
		this.remainingElements = limit;
	}
	
	public boolean hasNext() {
		return remainingElements > 0 && baseIterator.hasNext();
	}

	public E next() {
		remainingElements--;
		return baseIterator.next();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

}
