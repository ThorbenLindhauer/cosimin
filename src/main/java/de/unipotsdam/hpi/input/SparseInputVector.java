package de.unipotsdam.hpi.input;

import de.unipotsdam.hpi.sparse.DefaultSparseIntList;
import de.unipotsdam.hpi.sparse.SparseIntList;

public class SparseInputVector implements InputVector {

	private DefaultSparseIntList sparseIntList;
	private int id;
	
	public SparseInputVector(InputVector vector) {
		this(vector.getId(), (DefaultSparseIntList) vector.toSparseIntList());
	}

	public SparseInputVector(int id, DefaultSparseIntList sparseIntList) {
		this.id = id;
		this.sparseIntList = sparseIntList;
	}

	
	public int size() {
		return sparseIntList.size();
	}

	public int[] toIntArray() {
		throw new UnsupportedOperationException("Not implemented.");
	}

	public SparseIntList toSparseIntList() {
		return sparseIntList;
	}

	public int getId() {
		return id;
	}

	public static SparseInputVector fromBytes(int id, byte[] bytes) {
		return new SparseInputVector(id, DefaultSparseIntList.fromBytes(bytes));
	}
	

}
