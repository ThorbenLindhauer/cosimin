package de.unipotsdam.hpi.benchmark;

import java.io.IOException;

import de.unipotsdam.hpi.database.Settings;
import de.unipotsdam.hpi.database.VectorDatabase;
import de.unipotsdam.hpi.util.Profiler;

public class VectorDatabaseBulkLoadBenchmark extends AbstractVectorDatabaseBenchmark {

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
			System.out.println("Some stuff about usage and parameters");
			return;
		}

		Settings settings = new Settings();
		settings.load(args[0]);
		BenchmarkSettings benchmarkSettings = new BenchmarkSettings();
		benchmarkSettings.load(args[1]);

		new VectorDatabaseBulkLoadBenchmark(settings, benchmarkSettings).run();
	}
}
