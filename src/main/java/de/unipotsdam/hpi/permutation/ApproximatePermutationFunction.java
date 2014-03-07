package de.unipotsdam.hpi.permutation;

import java.util.Random;

@Deprecated
public class ApproximatePermutationFunction implements PermutationFunction {

	private static final long serialVersionUID = -8527728673857702553L;

	public static final int PERMUTATION_PRIME = 3001; 
	
	private int a, b, p;
	private int[] output;

	public ApproximatePermutationFunction(int numElements) {
		// TODO Generate p based on numElements
		p = PERMUTATION_PRIME;

		Random random = new Random();
		a = random.nextInt(p - 1) + 1;
		b = random.nextInt(p);
		
		output = new int[numElements];
		for (int i = 0; i < numElements; i++) {
			output[i] = (a * i + b) % p;
		}
	}
	
	public long[] permute(long[] signature) {
		throw new RuntimeException("Not implemented!");
	}

}
