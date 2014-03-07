package de.unipotsdam.hpi.indexing;

import java.io.Serializable;

abstract public class AbstractLinkedBlock implements LinkedBlock, Serializable {

	private static final long serialVersionUID = 2329004979646320986L;

	private LinkedBlock nextBlock;
	private LinkedBlock previousBlock;
	protected int capacity;
	protected int size;
	protected int keySize;
	protected long[] startKey;

	public LinkedBlock getNextBlock() {
		return nextBlock;
	}

	public void setNextBlock(LinkedBlock nextBlock) {
		this.nextBlock = nextBlock;
	}

	public LinkedBlock getPreviousBlock() {
		return previousBlock;
	}

	public void setPreviousBlock(LinkedBlock lastBlock) {
		this.previousBlock = lastBlock;
	}

	public int getCapacity() {
		return capacity;
	}

	public int getSize() {
		return size;
	}

	public long[] getStartKey() {
		return startKey;
	}

}
