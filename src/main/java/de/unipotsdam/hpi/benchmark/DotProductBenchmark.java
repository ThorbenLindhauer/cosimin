package de.unipotsdam.hpi.benchmark;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import de.unipotsdam.hpi.sparse.DefaultSparseIntList;
import de.unipotsdam.hpi.sparse.IntArraySparseIntList;
import de.unipotsdam.hpi.sparse.SparseIntList;
import de.unipotsdam.hpi.util.BoxMullerSampler;
import de.unipotsdam.hpi.util.Profiler;
import de.unipotsdam.hpi.util.RandomNumberSampler;

public class DotProductBenchmark {

  private static final Logger logger = Logger.getLogger(DotProductBenchmark.class.getName());
  
	private static final String NAIVE_DOT_PRODUCT = "Naive Dot Product";
	private static final String SPARSE_INT_LIST_DOT_PRODUCT = "Sparse Int List";
	
	private static final String NUM_VECTORS_KEY = "input.vectors.num";
	private static final String VECTOR_SIZE_KEY = "input.vectors.size";
	private static final String DENSITY_PERCENTAGE_KEY = "random.density.percentage";
	private static final String NUM_PRODUCTS_KEY = "products.num";
	
	private static final int SCALE_FACTOR = 100;
	
	private Properties properties;
	
	public DotProductBenchmark(String propertiesFilePath) {
		properties = new Properties();
		try {
			properties.load(new FileInputStream(propertiesFilePath));
		} catch (IOException e) {
			throw new IllegalArgumentException(
					"Could not load properties from " + propertiesFilePath);
		}
	}
	
	public void run() {
		// dense input vectors to multiply with
		int numVectors = Integer.parseInt(properties.getProperty(NUM_VECTORS_KEY));
		int vectorSize = Integer.parseInt(properties.getProperty(VECTOR_SIZE_KEY));
		int densityPercentage = Integer.parseInt(properties.getProperty(DENSITY_PERCENTAGE_KEY));
		int numProducts = Integer.parseInt(properties.getProperty(NUM_PRODUCTS_KEY));
		
		int[][] factorVectors = createRandomIntArrayVectors(numVectors, vectorSize);
		
		IntArraySparseIntList[] intArrayVectors = createRandomIntArraySparseIntListVectors(numVectors, vectorSize, densityPercentage);
		Profiler.start(NAIVE_DOT_PRODUCT);
		caluclateDotProducts(intArrayVectors, factorVectors, numProducts);
		Profiler.stop(NAIVE_DOT_PRODUCT);
		
		DefaultSparseIntList[] sparseIntVectors = createRandomDefaultSparseIntListVectors(numVectors, vectorSize, densityPercentage);
		Profiler.start(SPARSE_INT_LIST_DOT_PRODUCT);
		caluclateDotProducts(sparseIntVectors, factorVectors, numProducts);
		Profiler.stop(SPARSE_INT_LIST_DOT_PRODUCT);
		
		Profiler.printMeasurements();
	}
	
	private int[][] createRandomIntArrayVectors(int numVectors, int vectorSize) {
		int[][] vectors = new int[numVectors][vectorSize];
		RandomNumberSampler sampler = new BoxMullerSampler();

		for (int i = 0; i < numVectors; i++) {
			int[] vector = new int[vectorSize];
			for (int j = 0; j < vectorSize; j++) {
				double sampledValue = sampler.sample();
				vector[j] = (int) (SCALE_FACTOR * sampledValue);
			}
			vectors[i] = vector;
		}
		return vectors;
	}

	private void caluclateDotProducts(
			SparseIntList[] sparseVectors,
			int[][] factorVectors, int numProducts) {
		for (int i = 0; i < numProducts; i++) {
			sparseVectors[i % sparseVectors.length].scalarProduct(factorVectors[i % factorVectors.length]);
		}
		
	}

	private IntArraySparseIntList[] createRandomIntArraySparseIntListVectors(int numVectors, int vectorSize, int densityPercentage) {
		IntArraySparseIntList[] vectors = new IntArraySparseIntList[numVectors];
		RandomNumberSampler sampler = new BoxMullerSampler();
		Random random = new Random();

		for (int i = 0; i < numVectors; i++) {
			int[] vectorArray = new int[vectorSize];
			for (int j = 0; j < vectorSize; j++) {
				if (random.nextInt(100) >= densityPercentage)
					continue;
				double sampledValue = sampler.sample();
				vectorArray[j] = (int) (SCALE_FACTOR * sampledValue);
			}
			IntArraySparseIntList vector = new IntArraySparseIntList(vectorArray);
			vectors[i] = vector;
		}
		return vectors;
	}
	
	private DefaultSparseIntList[] createRandomDefaultSparseIntListVectors(int numVectors, int vectorSize, int densityPercentage) {
		DefaultSparseIntList[] vectors = new DefaultSparseIntList[numVectors];
		RandomNumberSampler sampler = new BoxMullerSampler();
		Random random = new Random();
		
		int estimatedSize = numVectors * (densityPercentage + 5) / 100;

		for (int i = 0; i < numVectors; i++) {
			DefaultSparseIntList vector = new DefaultSparseIntList(estimatedSize);
			vector.setSize(vectorSize);
			for (int j = 0; j < vectorSize; j++) {
				if (random.nextInt(100) >= densityPercentage)
					continue;
				double sampledValue = sampler.sample();
				vector.add(j, (int) (SCALE_FACTOR * sampledValue));
			}
			vectors[i] = vector;
		}
		return vectors;
	}
	

	public static void main(String[] args) {
		if (args.length < 1) {
			logger.severe("Some stuff about usage and parameters");
			return;
		}
		
		DotProductBenchmark benchmark = new DotProductBenchmark(args[0]);
		benchmark.run();
	}
}
