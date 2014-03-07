package de.unipotsdam.hpi.util;

import java.util.Random;

/**
 * Sampling based on the Box-Muller transformation.
 * @author Sebastian
 *
 */
public class BoxMullerSampler implements RandomNumberSampler {

	private Random rectangleSampler1 = new Random();
	private Random rectangleSampler2 = new Random();
	private double secondSample = Double.NaN;

	public double sample() {
		if (!Double.isNaN(secondSample)) {
			double result = secondSample;
			secondSample = Double.NaN;
			return result;
		}
		double u1 = rectangleSampler1.nextDouble();
		double u2 = rectangleSampler2.nextDouble();
		double x = Math.sqrt(-2 * Math.log(u1));
		double y = Math.PI * 2 * u2;
		secondSample = x * Math.cos(y);
		return x * Math.sin(y);
	}

}
