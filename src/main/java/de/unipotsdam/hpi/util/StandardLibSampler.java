package de.unipotsdam.hpi.util;

import java.util.Random;

public class StandardLibSampler implements RandomNumberSampler {

	private Random random = new Random();
	
	public double sample() {
		return random.nextGaussian();
	}

}
