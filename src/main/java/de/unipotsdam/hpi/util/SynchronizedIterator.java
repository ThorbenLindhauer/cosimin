package de.unipotsdam.hpi.util;

import java.util.Iterator;

public class SynchronizedIterator<E> implements Iterator<E> {

	private Iterator<E> iterator;

	public SynchronizedIterator(Iterator<E> iterator) {
		this.iterator = iterator;
	}

	synchronized public boolean hasNext() {
		return iterator.hasNext();
	}

	synchronized public E next() {
		return iterator.next();
	}

	synchronized public void remove() {
		iterator.remove();
	}
	
	
	
}
