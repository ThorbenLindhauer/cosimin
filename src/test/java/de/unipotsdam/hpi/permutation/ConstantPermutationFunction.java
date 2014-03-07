package de.unipotsdam.hpi.permutation;


public class ConstantPermutationFunction extends AbstractPermutationFunction {

	private static final long serialVersionUID = -6435630601968898574L;

	private int[] mapping;
	
	public ConstantPermutationFunction(int[] mapping) {
		this.mapping = mapping;
	}
	
	public long[] permute(long[] signature) {
		long[] permutation = new long[signature.length];
		createPermutation(this.mapping, signature, permutation);
		return permutation;
	}
	
}