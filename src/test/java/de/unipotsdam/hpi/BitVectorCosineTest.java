package de.unipotsdam.hpi;

import org.junit.Assert;
import org.junit.Test;

import de.unipotsdam.hpi.util.BitSignatureUtil;

public class BitVectorCosineTest {

  private double assertionTolerance = Double.MIN_VALUE;
  
	@Test
	public void testSelfCosine() {
		long[] signature = new long[]{ 1, 2, 64 };
		double cosine = BitSignatureUtil.calculateBitVectorCosine(signature, signature);
		Assert.assertEquals(1.0d, cosine, assertionTolerance);
	}
	
	@Test
	public void testMinimumCosine() {
		long[] signature1 = new long[]{ 1, 2, 64 }; 	// 00000001 00000010 01000000
		long[] signature2 = new long[]{ -2, -3, -65 };  // 11111110 11111101 10111111
		double cosine = BitSignatureUtil.calculateBitVectorCosine(signature1, signature2);
		Assert.assertEquals(-1.0d, cosine, assertionTolerance);
	}
	
	@Test
	public void testZeroCosine() {
		long[] signature1 = new long[]{ 0xFFFFFFFFL, 0xFFFFFFFFL, ~0xFFFFFFFFL }; 	// 00001111 00001111 11110000
		long[] signature2 = new long[]{ -1, -1, -1 };  // 11111111 11111111 11111111
		double cosine = BitSignatureUtil.calculateBitVectorCosine(signature1, signature2);
		
		// won't be exactly 0.0 because of double precision
		Assert.assertTrue(Math.abs(cosine) < 0.0001d);
	}
}
