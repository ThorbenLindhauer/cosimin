package de.unipotsdam.hpi.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ReservoirSampler<T> {

	private Random random = new Random();
	
	private int reservoirCapacity;
	private List<T> reservoir;
	private int seenElements;
	
	public ReservoirSampler(int reservoirCapacity) {
		this.reservoirCapacity = reservoirCapacity;
		this.reservoir = new ArrayList<T>(reservoirCapacity);
		this.seenElements = 0;
	}
	
	public void note(T element) {
		seenElements++;
		if (seenElements <= reservoirCapacity) {
			reservoir.add(element);
		} else {
			int randomIndex = random.nextInt(seenElements);
			if (randomIndex < reservoirCapacity) {
				reservoir.set(randomIndex, element);
			}
		}
	}
	
	public void shuffleReservoir() {
		Collections.shuffle(reservoir);
	}
	
	public List<T> getReservoir() {
		return reservoir;
	}
	
	public Iterator<T> wrapObserver(Iterator<T> iterator) {
		return new InterceptingIterator(iterator);
	}
	
	private class InterceptingIterator implements Iterator<T> {

		private Iterator<T> baseIterator;
		
		public InterceptingIterator(Iterator<T> baseIterator) {
			this.baseIterator = baseIterator;
		}

		public boolean hasNext() {
			return baseIterator.hasNext();
		}

		public T next() {
			T next = baseIterator.next();
			note(next);
			return next;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}

}
