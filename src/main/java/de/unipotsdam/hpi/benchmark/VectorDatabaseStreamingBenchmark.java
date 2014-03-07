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

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import de.unipotsdam.hpi.database.Settings;
import de.unipotsdam.hpi.database.VectorDatabase;
import de.unipotsdam.hpi.input.InputVector;
import de.unipotsdam.hpi.util.LimitingIterator;

/**
 * Due to the fact that not all input vectors are held in main memory, this
 * benchmark may be run with a higher number of input vectors.
 * 
 */
public class VectorDatabaseStreamingBenchmark extends
		AbstractVectorDatabaseBenchmark {

  private static final Logger logger = Logger.getLogger(VectorDatabaseStreamingBenchmark.class.getName());
  
	public VectorDatabaseStreamingBenchmark(Settings settings, BenchmarkSettings benchmarkSettings) {
		super(settings, benchmarkSettings);
	}

	@Override
	protected void loadVectorDb(VectorDatabase vectorDB) throws IOException {
		int vectorsPerChunk = benchmarkSettings.getStreamingChunkSize();
		while (true) {
			Iterator<InputVector> inputVectorIterator = new LimitingIterator<InputVector>(
					input, vectorsPerChunk);
			if (!inputVectorIterator.hasNext())
				break;
			vectorDB.submitInputVectors(inputVectorIterator);
		}

		vectorDB.create();
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
		  logger.info("Some stuff about usage and parameters");
			return;
		}

		Settings settings = new Settings();
		settings.load(args[0]);
		BenchmarkSettings benchmarkSettings = new BenchmarkSettings();
		benchmarkSettings.load(args[1]);

		new VectorDatabaseStreamingBenchmark(settings, benchmarkSettings).run();
	}
}
