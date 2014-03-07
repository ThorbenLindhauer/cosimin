package de.unipotsdam.hpi.util;

import java.util.Comparator;

public class BitSignatureUtil {
	
	public static int BASE_TYPE_SIZE = Long.SIZE;
	public static int LOG_BASE_TYPE_SIZE = 6; // 2^6 = 64
	
	public static int calculateSignatureSize(int numBits) {
		return (numBits + BitSignatureUtil.BASE_TYPE_SIZE - 1) >> BitSignatureUtil.LOG_BASE_TYPE_SIZE;
	}

	public static int calculateHammingDistance(long[] bitVector1, long[] bitVector2) {
		if (bitVector1.length != bitVector2.length)  {
			throw new RuntimeException("Cannot calculate Hamming Distance. Vectors are not of the same length: " + 
					"Vector 1: " + bitVector1.length + " elements. Vector 2: " +  bitVector2.length + " elements.");
		}
		
		int distance = 0;
		for (int i = 0; i < bitVector1.length; i++) {
			long xor = (bitVector1[i] ^ bitVector2[i]);
			distance += Long.bitCount(xor);
		}
		
		return distance;
	}

	public static double calculateBitVectorCosine(long[] bitVector1, long[] bitVector2) {
		int hammingDistance = calculateHammingDistance(bitVector1, bitVector2);
		double sameBitsRate = 1.0d - ((double) hammingDistance / ((double)(bitVector1.length << BitSignatureUtil.LOG_BASE_TYPE_SIZE)));
		
		double arg = (1.0d - sameBitsRate) * Math.PI;
		double cosineApproximation = Math.cos(arg);
		return cosineApproximation;
	}

	public static final Comparator<long[]> COMPARATOR = new Comparator<long[]>() {
	
		public int compare(long[] signature1, long[] signature2) {
			if (signature1.length != signature2.length) {
				throw new IllegalArgumentException(
						"Signatures must be of same length.");
			}
			
			for (int i = 0; i < signature1.length; i++) {
				// compare high bits
				int diff = Long.compare(signature1[i] >>> 1, signature2[i] >>> 1);
				if (diff != 0)
					return diff;

				// compare low bit
				diff = (int) ((signature1[i] & 1) - (signature2[i] & 1));
				if (diff != 0)
					return diff;
			}
	
			return 0;
		}
	};

}
