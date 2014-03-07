package de.unipotsdam.hpi.permutation;

import org.junit.Assert;
import org.junit.Test;

import de.unipotsdam.hpi.util.BitSignatureUtil;

public class PermutationTest {

	@Test
	public void testNoPermutation() {
		int[] mapping = new int[BitSignatureUtil.BASE_TYPE_SIZE];
		for (int i = 0; i < mapping.length; i++) {
			mapping[i] = i;
		}
		ConstantPermutationFunction permutationFunction = new ConstantPermutationFunction(
				mapping);
		long[] signature = { 1 + 4 + 16 };
		long[] permutation = permutationFunction.permute(signature);
		Assert.assertArrayEquals(signature, permutation);
	}

	@Test
	public void testReversePermutation() {
		int[] mapping = new int[BitSignatureUtil.BASE_TYPE_SIZE];
		for (int i = 0; i < mapping.length; i++) {
			mapping[i] = BitSignatureUtil.BASE_TYPE_SIZE - 1 - i;
		}
		ConstantPermutationFunction permutationFunction = new ConstantPermutationFunction(
				mapping);
		long[] signature = { (1L + 4L + 16L) << 56 };
		// 00010101 00000000 00000000 00000000 00000000 00000000 00000000
		// 00000000
		long[] expected = { 8L + 32L + 128L };
		// 00000000 00000000 00000000 00000000 00000000 00000000 00000000
		// 10101000
		long[] permutation = permutationFunction.permute(signature);
		Assert.assertArrayEquals(expected, permutation);
	}

	@Test
	public void testMultiBytePermutation() {
		int[] mapping = new int[2 * BitSignatureUtil.BASE_TYPE_SIZE];
		for (int i = 0; i < mapping.length; i++) {
			mapping[i] = 2 * BitSignatureUtil.BASE_TYPE_SIZE - 1 - i;
		}
		ConstantPermutationFunction permutationFunction = new ConstantPermutationFunction(
				mapping);
		long[] signature = {
				((1L + 2L + 16L + 32L) << 56) | ((1L + 2L + 4L + 8L) << 48),
				(1L + 4L + 16L + 64L) << 56 };
		// 00110011 00001111 0... | 01010101 0...
		long[] expected = { 128 + 32 + 8 + 2,
				((128 | 64 | 32 | 16) << 8) | (128 | 64 | 8 | 4) };
		// 0... 10101010 | 0... 11110000 11001100
		long[] permutation = permutationFunction.permute(signature);
		Assert.assertArrayEquals(expected, permutation);
	}

}
