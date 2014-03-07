package de.unipotsdam.hpi.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.unipotsdam.hpi.util.BitSignatureUtil;
import de.unipotsdam.hpi.util.Profiler;

public class BitSignatureBenchmark {

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
		System.out.println("Generating random bit signatures");
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
		System.out.format("Made %d comparisons\n", numComparisons);
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
		System.out.format("Made %d calculations\n", numComparisons);
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Some stuff about usage and parameters");
			return;
		}

		IndexBenchmarkSettings settings = new IndexBenchmarkSettings();
		settings.load(args[0]);

		new BitSignatureBenchmark(settings).run();
	}

}
