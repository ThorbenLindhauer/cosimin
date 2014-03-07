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

import java.util.Random;
import java.util.logging.Logger;

import de.unipotsdam.hpi.sorting.ParallelQuickSort;
import de.unipotsdam.hpi.sorting.SortAlgorithm;
import de.unipotsdam.hpi.sorting.StandardLibSort;
import de.unipotsdam.hpi.util.BitSignatureUtil;
import de.unipotsdam.hpi.util.Profiler;

public class SortingBenchmark {

  private static final Logger logger = Logger.getLogger(SortingBenchmark.class.getName());
  
	private IndexBenchmarkSettings settings;

	public SortingBenchmark(IndexBenchmarkSettings settings) {
		this.settings = settings;
	}

	private void run() {
		long[][] bitSignatures = generateBitSignatures();
		
		SortAlgorithm<long[]> sortAlgorithm = getSortAlgorithm();
		
		Profiler.start("Sorting");
		sortAlgorithm.sort(bitSignatures);
		Profiler.stop("Sorting");
		
		Profiler.printMeasurements();
	}

	private SortAlgorithm<long[]> getSortAlgorithm() {
		SortAlgorithm<long[]> sortAlgorithm;
		if (settings.isPerformParallelSorting()) {
			sortAlgorithm = new ParallelQuickSort<long[]>(10000); 
		} else {
			sortAlgorithm = new StandardLibSort<long[]>();
		}
		sortAlgorithm.setComparator(BitSignatureUtil.COMPARATOR);
		return sortAlgorithm;
	}
	
	private long[][] generateBitSignatures() {
	  logger.info("Generating random bit signatures");
		Profiler.start("Generating random bit signatures.");

		int numBitSignatures = settings.getNumBitSignatures();
		int bitSignatureSize = settings.getBitSignatureLength();
		int numLongs = bitSignatureSize >> BitSignatureUtil.LOG_BASE_TYPE_SIZE;
		Random random = new Random();

		long[][] bitSignatures = new long[numBitSignatures][];
		for (int i = 0; i < numBitSignatures; i++) {
			long[] bitSignature = new long[numLongs];
			for (int j = 0; j < numLongs; j++) {
				bitSignature[j] = random.nextLong();
			}
			bitSignatures[i] = bitSignature;
		}

		Profiler.stop("Generating random bit signatures.");
		return bitSignatures;
	}

	/**
	 * Runs this benchmark.
	 * 
	 * @param args
	 *            path to settings file
	 */
	public static void main(String[] args) {
	  IndexBenchmarkSettings settings = new IndexBenchmarkSettings();
		settings.load(args[0]);
		new SortingBenchmark(settings).run();
	}

}
