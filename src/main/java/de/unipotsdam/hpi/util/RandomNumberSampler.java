package de.unipotsdam.hpi.util;

/** A sampler that samples from the standard Gaussian distribution (0, 1). */
public interface RandomNumberSampler {
	
	/** Sample a new number. */
	double sample();

}
