package de.unipotsdam.hpi.permutation;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.Collections;

public class ListBasedPermutationFunction extends AbstractPermutationFunction {

	private static final long serialVersionUID = 2403060505967005297L;

	private int[] mapping;

	public ListBasedPermutationFunction(int numElements) {
		
		IntArrayList mappingList = new IntArrayList(numElements);
		for (int i = 0; i < numElements; i++) {
			mappingList.add(i);
		}
		Collections.shuffle(mappingList);
		mapping = mappingList.toIntArray();
	}
	
	public long[] permute(long[] signature) {
		long[] permutation = new long[signature.length];
		createPermutation(mapping, signature, permutation);
		return permutation;
	}
	
}
