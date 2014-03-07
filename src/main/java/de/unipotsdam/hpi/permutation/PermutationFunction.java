package de.unipotsdam.hpi.permutation;

import java.io.Serializable;

public interface PermutationFunction extends Serializable {

	long[] permute(long[] signature);
	
}
