package de.unipotsdam.hpi.benchmark;

import java.io.IOException;
import java.util.Iterator;

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
			System.out.println("Some stuff about usage and parameters");
			return;
		}

		Settings settings = new Settings();
		settings.load(args[0]);
		BenchmarkSettings benchmarkSettings = new BenchmarkSettings();
		benchmarkSettings.load(args[1]);

		new VectorDatabaseStreamingBenchmark(settings, benchmarkSettings).run();
	}
}
