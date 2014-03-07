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
import java.util.logging.Logger;

import de.unipotsdam.hpi.database.Settings;
import de.unipotsdam.hpi.database.VectorDatabase;
import de.unipotsdam.hpi.util.Profiler;

public class VectorDatabaseBulkLoadBenchmark extends AbstractVectorDatabaseBenchmark {

  private static final Logger logger = Logger.getLogger(VectorDatabaseBulkLoadBenchmark.class.getName());
  
	public VectorDatabaseBulkLoadBenchmark(Settings settings, BenchmarkSettings benchmarkSettings) {
		super(settings, benchmarkSettings);
	}

	@Override
	protected void loadVectorDb(VectorDatabase vectorDB) throws IOException {
		Profiler.start(PK_BULK_LOAD);
		vectorDB.bulkLoad(input);
		Profiler.stop(PK_BULK_LOAD);
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
		  logger.info("Some stuff about usage and parameters");
			return;
		}

		Settings settings = new Settings();
		settings.load(args[0]);
		BenchmarkSettings benchmarkSettings = new BenchmarkSettings();
		benchmarkSettings.load(args[1]);

		new VectorDatabaseBulkLoadBenchmark(settings, benchmarkSettings).run();
	}
}
