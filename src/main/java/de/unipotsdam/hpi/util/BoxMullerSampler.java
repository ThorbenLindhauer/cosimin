/*
 * Copyright 2014 Sebastian Kruse, Thorben Lindhauer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
