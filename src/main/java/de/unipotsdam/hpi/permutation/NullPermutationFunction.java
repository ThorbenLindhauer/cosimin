package de.unipotsdam.hpi.permutation;

public class NullPermutationFunction implements PermutationFunction {

	private static final long serialVersionUID = 4129466572078374540L;

	public long[] permute(long[] signature) {
		return signature;
	}

}
