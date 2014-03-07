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
