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

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import de.unipotsdam.hpi.database.Settings;
import de.unipotsdam.hpi.database.VectorDatabase;
import de.unipotsdam.hpi.indexing.CachingBlock;
import de.unipotsdam.hpi.input.InputVector;
import de.unipotsdam.hpi.input.IntArrayInputVector;
import de.unipotsdam.hpi.util.Profiler;
import de.unipotsdam.hpi.util.ReservoirSampler;

public abstract class AbstractVectorDatabaseBenchmark {

  private static final Logger logger = Logger.getLogger(AbstractVectorDatabaseBenchmark.class.getName());
  
	protected static final String PK_GET_VECTORS = "Get Vectors";
	protected static final String PK_BULK_LOAD = "Bulk Load";
	protected static final String PK_ASYNC_EXEC_QUERIES = "Execute async queries";
	protected static final String PK_SYNC_EXEC_QUERIES = "Execute sync queries";
	protected static final String PK_COMPLETE = "Complete runtime";

	protected Settings settings;
	protected BenchmarkSettings benchmarkSettings;
	protected Iterator<InputVector> input;
	protected ReservoirSampler<InputVector> queryVectorSampler;

	public AbstractVectorDatabaseBenchmark(Settings settings, BenchmarkSettings benchmarkSettings) {
		this.settings = settings;
		this.benchmarkSettings = benchmarkSettings;
	}

	protected void run() throws IOException {
		Profiler.start(PK_COMPLETE);

		int vectorSize = settings.getInputVectorSize();
		/*if (settings.getInputVectorFilePath() != null) {
			InputVectorStorage inputVectorStorage = new InputVectorStorage(
					settings.getInputVectorFilePath());
			input = inputVectorStorage.iterator();
			vectorSize = inputVectorStorage.getVectorSize();
		} else {}*/
		InputVectorSource inputVectorSource = new InputVectorSource(
				settings, benchmarkSettings);
		input = inputVectorSource.createGenerator();
		
		this.queryVectorSampler = new ReservoirSampler<InputVector>(
				benchmarkSettings.getNumQueries());
		input = queryVectorSampler.wrapObserver(input);

		settings.setInputVectorSize(vectorSize);
		VectorDatabase vectorDB = new VectorDatabase(settings);

		loadVectorDb(vectorDB);

		queryVectorSampler.shuffleReservoir();
		List<InputVector> queryVectors = getQueryVectors();
		if (benchmarkSettings.isMutateQueryVectors())
			mutateVectors(queryVectors);

		if (benchmarkSettings.isPerformSyncQueries())
			executeSyncRandomQueries(vectorDB, queryVectors);

		if (benchmarkSettings.isPerformAsyncQueries())
			executeAsyncRandomQueries(vectorDB, queryVectors);

		Profiler.stop(PK_COMPLETE);

		Profiler.printMeasurements();
		CachingBlock.printStatistics();
	}

	private void mutateVectors(List<InputVector> queryVectors) {
		Profiler.start("Mutate query vectors");
		for (int i = 0; i < queryVectors.size(); i++) {
			InputVector originalVector = queryVectors.get(i);
			InputVector mutatedVector = IntArrayInputVector.mutateVector(
					originalVector, benchmarkSettings.getVectorComponentRange(),
					Math.pow(Math.random(), 4));
			queryVectors.set(i, mutatedVector);
		}
		Profiler.stop("Mutate query vectors");
	}

	protected abstract void loadVectorDb(VectorDatabase vectorDB)
			throws IOException;

	protected List<InputVector> getInputVectors() {
		Profiler.start(PK_GET_VECTORS);
		List<InputVector> inputVectors = new ArrayList<InputVector>();
		while (input.hasNext()) {
			inputVectors.add(input.next());
		}
		Profiler.stop(PK_GET_VECTORS);

		for (InputVector inputVector : inputVectors) {
			queryVectorSampler.note(inputVector);
		}

		return inputVectors;
	}

	protected List<InputVector> getInputVectors(int limit) {
		Profiler.start(PK_GET_VECTORS);
		List<InputVector> inputVectors = new ArrayList<InputVector>();
		while (input.hasNext() && limit-- > 0) {
			inputVectors.add(input.next());
		}
		Profiler.stop(PK_GET_VECTORS);

		for (InputVector inputVector : inputVectors) {
			queryVectorSampler.note(inputVector);
		}

		return inputVectors;
	}

	protected List<InputVector> getQueryVectors() {
		return queryVectorSampler.getReservoir();
	}

	private void executeAsyncRandomQueries(VectorDatabase vectorDB,
			List<InputVector> baseVectors) {
	  logger.info("Executing async queries");

		ExecutorService executorService = Executors.newFixedThreadPool(benchmarkSettings.getNumQueryThreads());
		
		Profiler.start(PK_ASYNC_EXEC_QUERIES);
		List<BenchmarkQueryResult> results = new ArrayList<BenchmarkQueryResult>();

		for (InputVector baseVector : baseVectors) {
			InputVector queryVector = IntArrayInputVector.mutateVector(
					baseVector, benchmarkSettings.getVectorComponentRange(),
					Math.pow(Math.random(), 4));
			
			QueryHandler handler = new QueryHandler(vectorDB, queryVector, 
			    benchmarkSettings.getBeamSize(), benchmarkSettings.getMinSimilarity());

			Future<Int2DoubleMap> futureResult = executorService.submit(handler);
			BenchmarkQueryResult result = new BenchmarkQueryResult(
					baseVector.getId(), futureResult);
			results.add(result);
		}

		executorService.shutdown();

		for (BenchmarkQueryResult result : results) {
			try {
				@SuppressWarnings("unused")
				Int2DoubleMap resolvedResult = result.getFutureResult().get();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		Profiler.stop(PK_ASYNC_EXEC_QUERIES);

		String formatTime = Profiler.formatTime(Profiler
				.getTime(PK_ASYNC_EXEC_QUERIES) / benchmarkSettings.getNumQueries());
		logger.info("Average execution time: " + formatTime);
	}

	private void executeSyncRandomQueries(VectorDatabase vectorDB,
			List<InputVector> baseVectors) {
	  logger.info("Executing sync queries");

		for (InputVector baseVector : baseVectors) {
			// InputVector queryVector = IntArrayInputVector.mutateVector(
			// baseVector, settings.getVectorComponentRange(),
			// 0 /*Math.pow(Math.random(), 4)*/);
			InputVector queryVector = baseVector;

			Profiler.start(PK_SYNC_EXEC_QUERIES);
			Int2DoubleMap nearNeighborsWithDistance = vectorDB
					.getNearNeighborsWithDistance(queryVector,
					    benchmarkSettings.getBeamSize(), benchmarkSettings.getMinSimilarity());
			Profiler.stop(PK_SYNC_EXEC_QUERIES);

			logger.info(String.format("%d", baseVector.getId()));
			logger.info(nearNeighborsWithDistance.toString());
		}

		String formatTime = Profiler.formatTime(Profiler
				.getTime(PK_SYNC_EXEC_QUERIES) / benchmarkSettings.getNumQueries());
		logger.info("Average execution time: " + formatTime);
	}
}
