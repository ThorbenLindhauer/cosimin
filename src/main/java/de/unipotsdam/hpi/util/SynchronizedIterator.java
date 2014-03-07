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
