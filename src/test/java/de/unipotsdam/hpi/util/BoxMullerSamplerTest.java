package de.unipotsdam.hpi.util;

import org.junit.Assert;
import org.junit.Test;

public class BoxMullerSamplerTest {

	@Test
	public void testMean() {
		final double expectedMean = 0d;
		final double maxError = 0.03d;
		final int numOfSamples = 10000;

		RandomNumberSampler sampler = new BoxMullerSampler();
		double sum = 0d;
		for (int i = 0; i < numOfSamples; i++) {
			sum += sampler.sample();
		}
		double mean = sum / numOfSamples;
		String msg = String.format("Expected mean: %f, actual mean: %f",
				expectedMean, mean);
		Assert.assertTrue(msg, Math.abs(mean - expectedMean) < maxError);
	}
	
	@Test
	public void testVariance() {
		final double expectedVariance = 1d;
		final double maxError = 0.05d;
		final int numOfSamples = 10000;

		// Collect samples and calculate mean.
		RandomNumberSampler sampler = new BoxMullerSampler();
		double[] samples = new double[numOfSamples];
		double sum = 0d;
		for (int i = 0; i < numOfSamples; i++) {
			double sample = sampler.sample();
			sum += sample;
			samples[i] = sample;
		}
		double mean = sum / numOfSamples;
		
		sum = 0d;
		for (double sample : samples) {
			double delta = sample - mean;
			sum += delta * delta;
		}
		double variance = sum / numOfSamples;
		String msg = String.format("Expected variance: %f, actual variance: %f",
				expectedVariance, variance);
		Assert.assertTrue(msg, Math.abs(variance - expectedVariance) < maxError);		
	}

}
