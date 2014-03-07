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
package de.unipotsdam.hpi.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import de.unipotsdam.hpi.util.BitSignatureUtil;
import de.unipotsdam.hpi.util.Profiler;

public class BitSignatureBenchmark {

  private static final Logger logger = Logger.getLogger(BitSignatureBenchmark.class.getName());
  
	private IndexBenchmarkSettings settings;

	public BitSignatureBenchmark(IndexBenchmarkSettings settings) {
		this.settings = settings;
	}

	public void run() {
		List<long[]> bitSignatures = generateBitSignatures();
		compareBitSignatures(bitSignatures);
		calculateCosineSimilarities(bitSignatures);
		Profiler.printMeasurements();
	}

	private List<long[]> generateBitSignatures() {
	  logger.info("Generating random bit signatures");
		Profiler.start("Generating random bit signatures.");

		int numBitSignatures = settings.getNumBitSignatures();
		int bitSignatureSize = settings.getBitSignatureLength();
		int numLongs = bitSignatureSize >> BitSignatureUtil.LOG_BASE_TYPE_SIZE;
		Random random = new Random();

		List<long[]> bitSignatures = new ArrayList<long[]>(numBitSignatures);
		for (int i = 0; i < numBitSignatures; i++) {
			long[] bitSignature = new long[numLongs];
			for (int j = 0; j < numLongs; j++) {
				bitSignature[j] = random.nextLong();
			}
			bitSignatures.add(bitSignature);
		}

		Profiler.stop("Generating random bit signatures.");
		return bitSignatures;
	}

	private void compareBitSignatures(List<long[]> bitSignatures) {
		Profiler.start("Comparing bit signatures.");
		int numSignatures = bitSignatures.size();
		for (int i1 = 0; i1 < numSignatures - 1; i1++) {
			long[] signature1 = bitSignatures.get(i1);
			for (int i2 = i1; i2 < numSignatures; i2++) {
				long[] signature2 = bitSignatures.get(i2);
				BitSignatureUtil.COMPARATOR.compare(signature1, signature2);
			}
		}
		Profiler.stop("Comparing bit signatures.");
		int numComparisons = numSignatures
				* (numSignatures + 1) / 2;
		logger.info(String.format("Made %d comparisons\n", numComparisons));
	}
	
	private void calculateCosineSimilarities(List<long[]> bitSignatures) {
		Profiler.start("Calculating cosine similarities.");
		int numSignatures = bitSignatures.size();
		for (int i1 = 0; i1 < numSignatures - 1; i1++) {
			long[] signature1 = bitSignatures.get(i1);
			for (int i2 = i1; i2 < numSignatures; i2++) {
				long[] signature2 = bitSignatures.get(i2);
				BitSignatureUtil.calculateBitVectorCosine(signature1, signature2);
			}
		}
		Profiler.stop("Calculating cosine similarities.");
		int numComparisons = numSignatures
				* (numSignatures + 1) / 2;
		logger.info(String.format("Made %d calculations\n", numComparisons));
	}

	public static void main(String[] args) {
		if (args.length < 1) {
		  logger.info("Some stuff about usage and parameters");
			return;
		}

		IndexBenchmarkSettings settings = new IndexBenchmarkSettings();
		settings.load(args[0]);

		new BitSignatureBenchmark(settings).run();
	}

}
