package de.unipotsdam.hpi.permutation;

import de.unipotsdam.hpi.util.BitSignatureUtil;

public abstract class AbstractPermutationFunction implements
		PermutationFunction {

	private static final long serialVersionUID = 8518679840855246914L;

	private static final long MAX_BIT = 1L << (BitSignatureUtil.BASE_TYPE_SIZE - 1);
	
	/**
	 * 
	 * @param mapping
	 * @param input
	 * @param output has to be 0 initialized
	 */
	protected void createPermutation(int[] mapping, long[] input, long[] output) {
		int elementPos;
		int bitPos;

		for (int i = 0; i < mapping.length; i++) {
			
			// test bit i
			bitPos = i & BitSignatureUtil.BASE_TYPE_SIZE - 1;
			elementPos = i >> BitSignatureUtil.LOG_BASE_TYPE_SIZE;
			if ((input[elementPos] & (MAX_BIT >>> bitPos)) != 0) {
		
				// set bit in output
				int targetPosition = mapping[i];
				bitPos = targetPosition & (BitSignatureUtil.BASE_TYPE_SIZE - 1);
				elementPos = targetPosition >> BitSignatureUtil.LOG_BASE_TYPE_SIZE;
				output[elementPos] |= MAX_BIT >>> bitPos;
			}
		}
	}
	
}
