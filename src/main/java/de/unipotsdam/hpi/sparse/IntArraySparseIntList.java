package de.unipotsdam.hpi.sparse;



public class IntArraySparseIntList extends AbstractSparseIntList {

	private static final long serialVersionUID = 3258823691175424955L;

	private int[] array;

	public IntArraySparseIntList(int[] array) {
		this.array = array;
	}
	
	@Override
	public int size() {
		return array.length;
	}
	
	public Cursor createCursor() {
		return new Cursor() {
			
			int position = -1;
			
			public boolean move() {
				return ++position < array.length;
			}
			
			public int getValue() {
				return array[position];
			}
			
			public int getPosition() {
				return position;
			}
		};
	}

}
