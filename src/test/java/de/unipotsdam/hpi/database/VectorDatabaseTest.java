package de.unipotsdam.hpi.database;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import de.unipotsdam.hpi.input.InputVector;
import de.unipotsdam.hpi.input.IntArrayInputVector;
import de.unipotsdam.hpi.util.FileUtils;

public class VectorDatabaseTest {

  private static final int BEAM_SIZE = 1;
  private static final int VECTOR_COMPONENT_RANGE = 100;
  private static final double MIN_SIMILARITY = 1.0d;
  
  private static final String ROOT_TEMP_FOLDER_NAME = ".test/" + VectorDatabaseTest.class.getName();
  
	@Test
	public void testExactMatchesAlwaysFoundByBulkLoad() throws IOException {
		final int numInputVectors = 100;
		Settings settings = new Settings();
		settings.setInputVectorSize(10000);
		settings.setLshSize(1024);
		settings.setNumPermutations(1);
		settings.setBlockSize(20);
		settings.setBasePath(ROOT_TEMP_FOLDER_NAME + "/testExactMatchesAlwaysFoundByBulkLoad");
		settings.setSaveBitSignatures(false);
		settings.setPerformParallelLsh(false);
		settings.setPerformParallelSorting(false);

		List<InputVector> inputVectors = new ArrayList<InputVector>(
				numInputVectors);
		for (int i = 0; i < numInputVectors; i++) {
			inputVectors.add(IntArrayInputVector
					.generateInputRandomVector(settings.getInputVectorSize(), VECTOR_COMPONENT_RANGE));
		}

		VectorDatabase vdb = new VectorDatabase(settings);
		vdb.bulkLoad(inputVectors.iterator());

		for (int i = 0; i < numInputVectors; i++) {
			InputVector queryVector = inputVectors.get(i);
			Int2DoubleMap queryResult = vdb.getNearNeighborsWithDistance(
					queryVector, BEAM_SIZE, MIN_SIMILARITY);
			String msg = String.format("%s should contain vector %d",
					queryResult, queryVector.getId());
			Assert.assertTrue(msg, queryResult.containsKey(queryVector.getId()));
		}
	}

	@Test
	public void testExactMatchesAlwaysFoundByStreaming() throws IOException {
		final int numInputVectors = 100;
		Settings settings = new Settings();
		settings.setInputVectorSize(10000);
		settings.setLshSize(1024);
		settings.setNumPermutations(1);
		settings.setBlockSize(20);
		settings.setBasePath(ROOT_TEMP_FOLDER_NAME + "/testExactMatchesAlwaysFoundByStreaming");
		settings.setSaveBitSignatures(false);
		settings.setPerformParallelLsh(false);
		settings.setPerformParallelSorting(false);
		final int numVectorsPerChunk = 10;

		List<InputVector> inputVectors = new ArrayList<InputVector>(
				numInputVectors);
		for (int i = 0; i < numInputVectors; i++) {
			inputVectors.add(IntArrayInputVector
					.generateInputRandomVector(settings.getInputVectorSize(), VECTOR_COMPONENT_RANGE));
		}

		VectorDatabase vdb = new VectorDatabase(settings);

		// 'stream in' the vectors
		int numChunks = (numInputVectors + numVectorsPerChunk - 1)
				/ numVectorsPerChunk;
		for (int i = 0; i < numChunks; i++) {
			List<InputVector> chunk = inputVectors
					.subList(
							i * numVectorsPerChunk,
							Math.min(inputVectors.size(), (i + 1)
									* numVectorsPerChunk));
			vdb.submitInputVectors(chunk.iterator());
		}
		vdb.create();

		for (int i = 0; i < numInputVectors; i++) {
			InputVector queryVector = inputVectors.get(i);
			Int2DoubleMap queryResult = vdb.getNearNeighborsWithDistance(
					queryVector, BEAM_SIZE, MIN_SIMILARITY);
			String msg = String.format("%s should contain vector %d",
					queryResult, queryVector.getId());
			Assert.assertTrue(msg, queryResult.containsKey(queryVector.getId()));
		}
	}

	@Test
	public void testApproximateMatchesMostlyFound() throws IOException {
	  double minSimilarity = 0.8d;
	  int beamSize = 5;
	  
		final int numInputVectors = 100;
		Settings settings = new Settings();
		settings.setInputVectorSize(10000);
		settings.setLshSize(1024);
		settings.setNumPermutations(30);
		settings.setBlockSize(20);
		settings.setBasePath(ROOT_TEMP_FOLDER_NAME + "/testApproximateMatchesMostlyFound");
		settings.setSaveBitSignatures(false);
		settings.setPerformParallelLsh(false);
		settings.setPerformParallelSorting(false);
		final int minHits = 90;

		List<InputVector> inputVectors = new ArrayList<InputVector>(
				numInputVectors);
		for (int i = 0; i < numInputVectors; i++) {
			inputVectors.add(IntArrayInputVector
					.generateInputRandomVector(settings.getInputVectorSize(), VECTOR_COMPONENT_RANGE));
		}

		VectorDatabase vdb = new VectorDatabase(settings);
		vdb.bulkLoad(inputVectors.iterator());

		int hits = 0;
		for (int i = 0; i < numInputVectors; i++) {
			InputVector inputVector = inputVectors.get(i);
			InputVector queryVector = IntArrayInputVector.mutateVector(
					inputVector, VECTOR_COMPONENT_RANGE, 0.05d);
			Int2DoubleMap queryResult = vdb.getNearNeighborsWithDistance(
					queryVector, beamSize, minSimilarity);
			if (queryResult.containsKey(inputVector.getId()))
				hits++;
		}
		String msg = String.format(
				"Expected at least %d hits, but encountered only %d", minHits,
				hits);
		Assert.assertTrue(msg, hits >= minHits);
	}
	
	@Test
	public void testExactMatchesAlwaysFoundAfterRecovery() throws IOException {
		final int numInputVectors = 100;
		Settings settings = new Settings();
		settings.setInputVectorSize(10000);
		settings.setLshSize(1024);
		settings.setNumPermutations(1);
		settings.setBlockSize(20);
		settings.setBasePath(ROOT_TEMP_FOLDER_NAME + "/testExactMatchesAlwaysFoundAfterRecovery");
		settings.setSaveBitSignatures(false);
		settings.setPerformParallelLsh(false);
		settings.setPerformParallelSorting(false);
		
		List<InputVector> inputVectors = new ArrayList<InputVector>(
				numInputVectors);
		for (int i = 0; i < numInputVectors; i++) {
			inputVectors.add(IntArrayInputVector
					.generateInputRandomVector(settings.getInputVectorSize(), VECTOR_COMPONENT_RANGE));
		}
		
		VectorDatabase vdb = new VectorDatabase(settings);
		vdb.bulkLoad(inputVectors.iterator());
		
		vdb = new VectorDatabase(settings);
		vdb.recover();
		
		for (int i = 0; i < numInputVectors; i++) {
			InputVector queryVector = inputVectors.get(i);
			Int2DoubleMap queryResult = vdb.getNearNeighborsWithDistance(
					queryVector, BEAM_SIZE, MIN_SIMILARITY);
			String msg = String.format("%s should contain vector %d",
					queryResult, queryVector.getId());
			Assert.assertTrue(msg, queryResult.containsKey(queryVector.getId()));
		}
	}
	
	@AfterClass
	public static void cleanUp() {
	  Path path = FileSystems.getDefault().getPath(ROOT_TEMP_FOLDER_NAME);
	  FileUtils.clearAndDeleteDirecotry(path);
	}


}
