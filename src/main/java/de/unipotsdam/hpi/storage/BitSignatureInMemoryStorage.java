package de.unipotsdam.hpi.storage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.unipotsdam.hpi.indexing.IndexPair;

public class BitSignatureInMemoryStorage implements BitSignatureStorage {

	private List<IndexPair> signatures = new ArrayList<IndexPair>();
	
	public Iterator<IndexPair> iterator() {
		return signatures.iterator();
	}

	synchronized public void store(IndexPair indexPair) {
		signatures.add(indexPair);
	}

	public void clear() {
		signatures.clear();
	}

	public void flushOutput() {
	}

	public void closeOutput() {
	}

}
