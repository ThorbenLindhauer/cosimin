package de.unipotsdam.hpi.sparse;

abstract public class AbstractSparseIntList implements SparseIntList {

	private static final long serialVersionUID = 4003864950859590544L;

	public abstract int size();

	public long scalarProduct(SparseIntList other) {
		SparseIntList.Cursor i1 = createCursor();
		SparseIntList.Cursor i2 = other.createCursor();

		long scalarProduct = 0;
		boolean moveI1, moveI2;

		boolean isI1Valid = i1.move();
		boolean isI2Valid = i2.move();
		while (isI1Valid && isI2Valid) {
			moveI1 = i1.getPosition() <= i2.getPosition();
			moveI2 = i2.getPosition() <= i1.getPosition();
			if (moveI1 && moveI2) {
				scalarProduct += i1.getValue() * i2.getValue();
			}
			if (moveI1) {
				isI1Valid = i1.move();
			}
			if (moveI2) {
				isI2Valid = i2.move();
			}
		}

		return scalarProduct;
	}

	public long scalarProduct(int[] other) {
		if (other.length != size()) {
			throw new IllegalArgumentException("Vector size " + other.length
					+ ", expected " + size());
		}

		long scalarProduct = 0;
		SparseIntList.Cursor cursor = createCursor();
		while (cursor.move()) {
			int position = cursor.getPosition();
			int value = cursor.getValue();
			int otherValue = other[position];
			scalarProduct += (long) otherValue * (long) value;
		}
		return scalarProduct;
	}

}
