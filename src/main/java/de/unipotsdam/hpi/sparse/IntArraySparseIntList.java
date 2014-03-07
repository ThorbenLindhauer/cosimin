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
