package de.unipotsdam.hpi.benchmark;

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

	private static final String PROF_KEY_LSH_CALCULATION = "LSH calculation";
	private Settings settings;
	private BenchmarkSettings benchmarkSettings;

	public LshBenchmark(Settings settings, BenchmarkSettings benchmarkSettings) {
	  this.benchmarkSettings = benchmarkSettings;
		this.settings = settings;
	}

	public void run() {
		System.out.println("Generate LSH function...");
		LshFunction lshFunction = LshFunction.createRandomLSH(settings.getLshSize(), settings.getInputVectorSize());

		System.out
				.println("Push random input vectors through the LSH function...");
		calculateLsh(lshFunction);

		long avgLshTime = Profiler.getTime(PROF_KEY_LSH_CALCULATION)
				/ benchmarkSettings.getNumInputVectors();
		System.out.println("Average LSH calculation time: "
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
				System.out.println("  " + ((i + 1) * 10 / debugModulo) + "%");
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
