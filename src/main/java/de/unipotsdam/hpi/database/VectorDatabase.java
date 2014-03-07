package de.unipotsdam.hpi.database;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.unipotsdam.hpi.indexing.BlockBasedIndex;
import de.unipotsdam.hpi.indexing.Index;
import de.unipotsdam.hpi.indexing.IndexPair;
import de.unipotsdam.hpi.input.InputVector;
import de.unipotsdam.hpi.lsh.LshFunction;
import de.unipotsdam.hpi.permutation.ListBasedPermutationFunction;
import de.unipotsdam.hpi.permutation.NullPermutationFunction;
import de.unipotsdam.hpi.permutation.PermutationFunction;
import de.unipotsdam.hpi.sorting.ParallelQuickSort;
import de.unipotsdam.hpi.sorting.SortAlgorithm;
import de.unipotsdam.hpi.sorting.StandardLibSort;
import de.unipotsdam.hpi.storage.BitSignatureDiskStorage;
import de.unipotsdam.hpi.storage.BitSignatureInMemoryStorage;
import de.unipotsdam.hpi.storage.BitSignatureStorage;
import de.unipotsdam.hpi.util.BitSignatureUtil;
import de.unipotsdam.hpi.util.FileUtils;
import de.unipotsdam.hpi.util.Profiler;

/**
 * This class encapsulates a scalable, queryable store for high-dimensional vectors.
 * @see LshFunction
 * @see Index
 */
public class VectorDatabase {

  // recovery file names
	private static final String INDEXES_FILE = "indexes.ser";
	private static final String LSH_FUNCTION_FILE = "lsh-func.ser";
	private static final String PERMUTATION_FUNCTIONS_FILE = "permutation-funcs.ser";
	private static final String VECTOR_DB_PROPERTIES = "vector-db.properties";
	
	// profiling keys
	private static final String PK_LSH_GENERATION = "Generate LSH function";
	private static final String PK_INDEX_CREATION = "Create Index";
	private static final String PK_SIGNATURE_SORTING = "Sort signatures";
	private static final String PK_SIGNATURE_STORE_LOADING = "Load from signature store";
	private static final String PK_SIGNATURE_STORE_SAVING = "Save to signature store";
	private static final String PK_PERMUTING = "Permute signatures";
	private static final String PK_LOAD_AND_LSH = "Load and hash vectors";
	
	// storage sub paths
	private static final String INDEX_PATH = "index";
	private static final String SIGNATURE_STORAGE_PATH = "signature_storage";

	private int bitSignatureSize;
	private int blockSize;
	private int numPermutations;
	private String basePath;
	private boolean saveBitSignatures;

	private LshFunction lshFunction;
	private BitSignatureStorage signatureStorage;
	private PermutationFunction[] permutationFunctions;
	private Index[] indexes;
	private Path storagePath;
	private Path indexPath;
	private int size;
	private boolean isInitializedForCreate = false;

	private boolean performParallelLsh;
	private boolean performParallelSorting;
	private Settings settings;
  private int vectorSize;

	public VectorDatabase(Settings settings) throws IOException {
		this.size = 0;
		applySettings(settings);
	}

	private void applySettings(Settings settings) throws IOException {
		this.settings = settings;
		this.bitSignatureSize = settings.getLshSize();
		this.blockSize = settings.getBlockSize();
		this.numPermutations = settings.getNumPermutations();
		this.basePath = settings.getBasePath();
		this.saveBitSignatures = settings.isSaveBitSignatures();
		this.performParallelLsh = settings.isPerformParallelLsh();
		this.performParallelSorting = settings.isPerformParallelSorting();
		this.vectorSize = settings.getInputVectorSize();

		if (bitSignatureSize % BitSignatureUtil.BASE_TYPE_SIZE != 0) {
			System.out.println("Warning: Non-aligned bit-signature size: "
					+ bitSignatureSize + "!");
		}
	}

	private void ensureInitialized(InputVector inputVector) {
		if (isInitializedForCreate) {
			return;
		}

		initializeForCreate();
	}

	private void initializeForCreate() {
		try {

			// Paths
			Path basePath = FileUtils.toPath(this.basePath);
			storagePath = basePath.resolve(SIGNATURE_STORAGE_PATH);
			indexPath = basePath.resolve(INDEX_PATH);
			FileUtils.createDirectoryIfNotExists(basePath);
			FileUtils.createDirectoryIfNotExists(storagePath);
			FileUtils.createDirectoryIfNotExists(indexPath);

			// LSH function
			Profiler.start(PK_LSH_GENERATION);
			lshFunction = LshFunction.createRandomLSH(bitSignatureSize,
					vectorSize);
			Profiler.stop(PK_LSH_GENERATION);

			// Signature store
			signatureStorage = saveBitSignatures ? new BitSignatureDiskStorage(
					basePath, bitSignatureSize, false)
					: new BitSignatureInMemoryStorage();

			// Permutation functions
			permutationFunctions = new PermutationFunction[numPermutations];
			for (int i = 0; i < numPermutations; i++) {
				if (i == 0)
					permutationFunctions[i] = new NullPermutationFunction();
				else
					permutationFunctions[i] = new ListBasedPermutationFunction(
							bitSignatureSize);
			}

			isInitializedForCreate = true;
		} catch (IOException e) {
			throw new RuntimeException("Could not initialize!", e);
		}
	}

	/**
	 * Use this method as a shorthand, if inputVectors iterates over ALL input
	 * vectors. Immediately creates the index.
	 * 
	 * @param inputVectors
	 * @throws IOException
	 */
	public void bulkLoad(Iterator<InputVector> inputVectors) throws IOException {
		createBitSignatures(inputVectors);
		create();
	}

	/**
	 * Use this method to submit input vectors (and store their bit signatures),
	 * but not yet create the indexes. Should be used to 'stream in' input
	 * vectors.
	 */
	public void submitInputVectors(Iterator<InputVector> inputVectors) {
		createBitSignatures(inputVectors);
	}

	private void createBitSignatures(Iterator<InputVector> inputVectors) {

		if (performParallelLsh)
			createBitSignaturesInParallel(inputVectors);
		else
			createBitSignaturesSequentially(inputVectors);
	}

	private void createBitSignaturesInParallel(
			Iterator<InputVector> inputVectors) {
		Profiler.start(PK_LOAD_AND_LSH);
		ExecutorService executor = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors());
		while (inputVectors.hasNext()) {
			final InputVector inputVector = inputVectors.next();
			ensureInitialized(inputVector);
			size++;
			executor.execute(new Runnable() {

				public void run() {
					long[] signature = lshFunction.createSignature(inputVector);
					int id = inputVector.getId();
					IndexPair indexPair = new IndexPair(signature, id);

					// Profiler.start(PK_SIGNATURE_STORE_SAVING);
					signatureStorage.store(indexPair);
					// Profiler.stop(PK_SIGNATURE_STORE_SAVING);

					if (size % 20000 == 0) {
						System.out.println("Processed " + size + " elements.");
					}

				}

			});

		}

		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		Profiler.stop(PK_LOAD_AND_LSH);

		Profiler.start(PK_SIGNATURE_STORE_SAVING);
		signatureStorage.flushOutput();
		Profiler.stop(PK_SIGNATURE_STORE_SAVING);
	}

	private void createBitSignaturesSequentially(
			Iterator<InputVector> inputVectors) {
		while (inputVectors.hasNext()) {
			final InputVector inputVector = inputVectors.next();
			ensureInitialized(inputVector);
			final int id = inputVector.getId();
			size++;
			Profiler.start(PK_LOAD_AND_LSH);
			long[] signature = lshFunction.createSignature(inputVector);
			Profiler.stop(PK_LOAD_AND_LSH);

			IndexPair indexPair = new IndexPair(signature, id);

			Profiler.start(PK_SIGNATURE_STORE_SAVING);
			signatureStorage.store(indexPair);
			Profiler.stop(PK_SIGNATURE_STORE_SAVING);

			if (size % 20000 == 0) {
				System.out.println("Processed " + size + " elements.");
			}

		}

		Profiler.start(PK_SIGNATURE_STORE_SAVING);
		signatureStorage.flushOutput();
		Profiler.stop(PK_SIGNATURE_STORE_SAVING);
	}

	public void create() throws IOException {
		signatureStorage.closeOutput();

		createIndexes();
		storeRecoverInformation();
	}

	/**
	 * Creates indexes for the current bit signatures.
	 * 
	 * @throws IOException
	 */
	private void createIndexes() throws IOException {

		FileUtils.clearDirectory(indexPath);
		Path[] indexPaths = FileUtils.getPaths(indexPath, numPermutations,
				"index");

		IndexPair[] permutatedElements = new IndexPair[size];
		indexes = new Index[numPermutations];
		int keySize = BitSignatureUtil.calculateSignatureSize(bitSignatureSize);

		for (int i = 0; i < numPermutations; i++) {
			permuteElements(permutatedElements, i);
			sortElements(permutatedElements);
			buildIndex(indexPaths, permutatedElements, keySize, i);
		}

		FileUtils.save(indexes, FileUtils.toPath(basePath, INDEXES_FILE));
	}

	private void permuteElements(IndexPair[] permutatedElements, int i) {
		PermutationFunction permutationFunction = permutationFunctions[i];

		int signatureNum = 0;
		Profiler.start(PK_SIGNATURE_STORE_LOADING);
		for (IndexPair pair : signatureStorage) {
			Profiler.stop(PK_SIGNATURE_STORE_LOADING);

			Profiler.start(PK_PERMUTING);
			long[] permutedSignature = permutationFunction.permute(pair
					.getBitSignature());
			Profiler.stop(PK_PERMUTING);

			permutatedElements[signatureNum] = new IndexPair(permutedSignature,
					pair.getElementId());
			signatureNum++;

			Profiler.start(PK_SIGNATURE_STORE_LOADING);
		}
		Profiler.stop(PK_SIGNATURE_STORE_LOADING);
	}

	private void sortElements(IndexPair[] permutatedElements) {
		SortAlgorithm<IndexPair> sortAlgorithm;
		if (performParallelSorting) {
			sortAlgorithm = new ParallelQuickSort<IndexPair>(10000);
		} else {
			sortAlgorithm = new StandardLibSort<IndexPair>();
		}
		sortAlgorithm.setComparator(IndexPair.COMPARATOR);

		Profiler.start(PK_SIGNATURE_SORTING);
		sortAlgorithm.sort(permutatedElements);
		Profiler.stop(PK_SIGNATURE_SORTING);

		sortAlgorithm.close();
	}

	private void buildIndex(Path[] indexPaths, IndexPair[] permutatedElements,
			int keySize, int i) {
		Profiler.start(PK_INDEX_CREATION);
		Index index = new BlockBasedIndex(indexPaths[i], keySize, blockSize);
		index.bulkLoad(permutatedElements);
		indexes[i] = index;
		Profiler.stop(PK_INDEX_CREATION);
		System.out.println("Created index " + i + ".");
	}

	private void storeRecoverInformation() throws FileNotFoundException,
			IOException {
		Properties properties = settings.toProperties();
		FileOutputStream fos = new FileOutputStream(new File(basePath,
				VECTOR_DB_PROPERTIES));
		properties.store(fos, "Do not mess with this file!");
		fos.close();

		String recoveryPath = FileUtils.toPath(basePath,
				PERMUTATION_FUNCTIONS_FILE).toString();
		FileUtils.save(permutationFunctions, recoveryPath);

		recoveryPath = FileUtils.toPath(basePath, LSH_FUNCTION_FILE).toString();
		FileUtils.save(lshFunction, recoveryPath);
	}

	public void recover() throws IOException {
		loadRecoverInformation();
		recoverIndex();
	}

	private void recoverIndex() throws IOException {
		indexes = (Index[]) FileUtils.load(FileUtils.toPath(basePath,
				INDEXES_FILE));
		for (Index index : indexes) {
			index.recover();
		}
	}

	private void loadRecoverInformation() throws IOException {
		Settings settings = new Settings();
		String recoverPath = FileUtils.toPath(basePath, VECTOR_DB_PROPERTIES)
				.toString();
		settings.load(recoverPath);
		applySettings(settings);

		recoverPath = FileUtils.toPath(basePath, PERMUTATION_FUNCTIONS_FILE)
				.toString();
		permutationFunctions = (PermutationFunction[]) FileUtils
				.load(recoverPath);

		recoverPath = FileUtils.toPath(basePath, LSH_FUNCTION_FILE).toString();
		lshFunction = (LshFunction) FileUtils.load(recoverPath);

		System.out.println("LshFunction: " + lshFunction.toString());
	}

	public void deleteFiles() {
		storagePath.toFile().delete();
		indexPath.toFile().delete();
	}

	public Int2DoubleMap getNearNeighborsWithDistance(InputVector queryVector,
			int beamRadius, double minSimilarity) {
		System.out.println("Query with beam " + beamRadius
				+ " and min similarity " + minSimilarity);
		Profiler.start("Find nearest neighbors");

		// create the signature of the input vector
		long[] querySignature = lshFunction.createSignature(queryVector);

		// create all permutations of this signature...
		Int2DoubleMap distances = new Int2DoubleOpenHashMap();
		IntSet seenElements = new IntOpenHashSet();
		for (int i = 0; i < numPermutations; i++) {
			PermutationFunction permutationFunction = permutationFunctions[i];
			Index index = indexes[i];
			long[] permutedSignature = permutationFunction
					.permute(querySignature);

			// ...and get the nearest neighbors from the corresponding index
			IndexPair[] neighbors = index.getNearestNeighbours(
					permutedSignature, beamRadius);

			// store the neighbors that are close enough
			for (IndexPair neighbor : neighbors) {
				// TODO remove if not needed
				// if (neighbor.getElementId() == queryVector.getId())
				// System.out.println("");
				if (seenElements.add(neighbor.getElementId())) {
					double similarity = BitSignatureUtil
							.calculateBitVectorCosine(permutedSignature,
									neighbor.getBitSignature());
					if (similarity >= minSimilarity)
						distances.put(neighbor.getElementId(), similarity);
				}
			}
		}

		Profiler.stop("Find nearest neighbors");
		return distances;
	}

}
