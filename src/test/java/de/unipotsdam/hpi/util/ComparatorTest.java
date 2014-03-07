package de.unipotsdam.hpi.util;

import java.util.Comparator;

import org.junit.Assert;
import org.junit.Test;


public class ComparatorTest {

	@Test
	public void testSignatureComparison() {
		long[] signature1 = new long[]{ 1, 2, 64 }; 	// 00000001 00000010 01000000
		long[] signature2 = new long[]{ -2, -3, -65 };  // 11111110 11111101 10111111
		
		Comparator<long[]> comparator = BitSignatureUtil.COMPARATOR;
		int comparison = comparator.compare(signature1, signature1);
		Assert.assertEquals(0, comparison);
		
		comparison = comparator.compare(signature1, signature2);
		Assert.assertTrue(comparison < 0);
		
		comparison = comparator.compare(signature2, signature1);
		Assert.assertTrue(comparison > 0);
	}
	
	@Test
	public void testSameSignSignatureComparison() {
		long[] signature1 = new long[]{ 1, 2, 64 }; 	// 00000001 00000010 01000000
		long[] signature2 = new long[]{ 3, -3, -65 };   // 00000011 11111101 10111111
		
		Comparator<long[]> comparator = BitSignatureUtil.COMPARATOR;
		int comparison = comparator.compare(signature1, signature1);
		Assert.assertEquals(0, comparison);
		
		comparison = comparator.compare(signature1, signature2);
		Assert.assertTrue(comparison < 0);
		
		comparison = comparator.compare(signature2, signature1);
		Assert.assertTrue(comparison > 0);
	}
	
}
