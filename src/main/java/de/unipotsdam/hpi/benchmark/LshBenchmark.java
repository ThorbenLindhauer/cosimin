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

import java.util.logging.Logger;

import de.unipotsdam.hpi.database.Settings;
import de.unipotsdam.hpi.input.InputVector;
import de.unipotsdam.hpi.input.IntArrayInputVector;
import de.unipotsdam.hpi.lsh.LshFunction;
import de.unipotsdam.hpi.util.Profiler;

/**
 * Benchmarks only LSH operations, i.e. primarily the hashing.
 * 
 * @author Sebastian
 * 
 */
public class LshBenchmark {

  private static final Logger logger = Logger.getLogger(LshBenchmark.class.getName());
  
	private static final String PROF_KEY_LSH_CALCULATION = "LSH calculation";
	private Settings settings;
	private BenchmarkSettings benchmarkSettings;

	public LshBenchmark(Settings settings, BenchmarkSettings benchmarkSettings) {
	  this.benchmarkSettings = benchmarkSettings;
		this.settings = settings;
	}

	public void run() {
	  logger.info("Generate LSH function...");
		LshFunction lshFunction = LshFunction.createRandomLSH(settings.getLshSize(), settings.getInputVectorSize());

		System.out
				.println("Push random input vectors through the LSH function...");
		calculateLsh(lshFunction);

		long avgLshTime = Profiler.getTime(PROF_KEY_LSH_CALCULATION)
				/ benchmarkSettings.getNumInputVectors();
		logger.info("Average LSH calculation time: "
				+ Profiler.formatTime(avgLshTime));
		Profiler.printMeasurements();
	}

	private void calculateLsh(LshFunction lshFunction) {
		int debugModulo = Math.max(benchmarkSettings.getNumInputVectors() / 10, 1);
		for (int i = 0; i < benchmarkSettings.getNumInputVectors(); i++) {
			InputVector vector = IntArrayInputVector
					.generateInputRandomVector(settings.getInputVectorSize(), 
					    benchmarkSettings.getVectorComponentRange());
			Profiler.start(PROF_KEY_LSH_CALCULATION);
			lshFunction.createSignature(vector);
			Profiler.stop(PROF_KEY_LSH_CALCULATION);
			if ((i + 1) % debugModulo == 0)
			  logger.info("  " + ((i + 1) * 10 / debugModulo) + "%");
		}
	}

	/**
	 * Runs this benchmark.
	 * 
	 * @param args
	 *            path to settings file
	 */
	public static void main(String[] args) {
		Settings settings = new Settings();
		settings.load(args[0]);
		BenchmarkSettings benchmarkSettings = new BenchmarkSettings();
		new LshBenchmark(settings, benchmarkSettings).run();
	}

}
