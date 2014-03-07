package de.unipotsdam.hpi.indexing;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.unipotsdam.hpi.util.FileUtils;
import de.unipotsdam.hpi.util.TestSettings;

public class BlockBasedIndexTest {

	private static final String TMP_FOLDER = BlockBasedIndexTest.class
			.getName();
	private static Path tempFolder;

	@BeforeClass
	public static void createTempDirectory() throws IOException {
		Path globalTempFolder = FileSystems.getDefault().getPath(
				TestSettings.INDEX_TMP_FOLDER);
		tempFolder = globalTempFolder.resolve(TMP_FOLDER);
		FileUtils.createDirectoryIfNotExists(globalTempFolder);
		FileUtils.createDirectoryIfNotExists(tempFolder);

		System.out.println("Using temporary folder " + tempFolder);
	}

	@Test
	public void testIndexBulkLoadingInsertsElements() {
		int blockSize = 100;
		int keySize = 4;
		int numIndexPairs = 300;
		BlockBasedIndex index = new BlockBasedIndex(tempFolder, keySize,
				blockSize);

		IndexPair[] indexPairs = new IndexPair[numIndexPairs];
		for (int i = 0; i < numIndexPairs; i++) {
			byte signatureByte0 = (byte) (i % 100);
			byte signatureByte1 = (byte) ((i / 100) % 100);
			long[] key = new long[] { 0, 0, signatureByte1, signatureByte0 };
			IndexPair pair = new IndexPair(key, i);
			indexPairs[i] = pair;
		}

		try {
			index.bulkLoad(indexPairs);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

		// There must be numIndexPairs elements in the index
		Assert.assertEquals(numIndexPairs, index.size());

		// Check all the values.
		int i = 0;
		for (IndexPair indexPair : index) {
			Assert.assertEquals(indexPairs[i], indexPair);
			i++;
		}
	}

	@Test
	public void testGetExistingElement() {
		int blockSize = 100;
		int keySize = 4;
		int numIndexPairs = 300;
		BlockBasedIndex index = new BlockBasedIndex(tempFolder, keySize,
				blockSize);

		IndexPair[] indexPairs = new IndexPair[numIndexPairs];
		for (int i = 0; i < numIndexPairs; i++) {
			byte signatureByte0 = (byte) (i % 100);
			byte signatureByte1 = (byte) ((i / 100) % 100);
			long[] key = new long[] { 0, 0, signatureByte1, signatureByte0 };
			IndexPair pair = new IndexPair(key, i);
			indexPairs[i] = pair;
		}
		Arrays.sort(indexPairs, IndexPair.COMPARATOR);
		index.bulkLoad(indexPairs);

		// Check all the values.
		for (IndexPair indexPair : indexPairs) {
			int retrievedElement = index
					.getElement(indexPair.getBitSignature());
			Assert.assertEquals(indexPair.getElementId(), retrievedElement);
		}
	}

	@Test
	public void testGetNonExistingElement() {
		int blockSize = 100;
		int keySize = 4;
		int numIndexPairs = 300;
		BlockBasedIndex index = new BlockBasedIndex(tempFolder, keySize,
				blockSize);

		IndexPair[] indexPairs = new IndexPair[numIndexPairs];
		for (int i = 0; i < numIndexPairs; i++) {
			byte signatureByte0 = (byte) (i % 100);
			byte signatureByte1 = (byte) ((i / 100) % 100);
			long[] key = new long[] { 0, 0, signatureByte1, signatureByte0 };
			IndexPair pair = new IndexPair(key, i);
			indexPairs[i] = pair;
		}
		Arrays.sort(indexPairs, IndexPair.COMPARATOR);
		index.bulkLoad(indexPairs);

		try {
			index.getElement(new long[] { 3, 2, 1, 0 });
			Assert.fail("Expected exception.");
		} catch (IllegalArgumentException e) {
			// happy path
		}
	}

	@Test
	public void testInsertSingleElement() throws IOException {
		// Test adding a single element that should not require block splitting.
		int blockSize = 100;
		int keySize = 4;
		int numIndexPairs = 300;
		BlockBasedIndex index = new BlockBasedIndex(tempFolder, keySize,
				blockSize);

		IndexPair[] indexPairs = new IndexPair[numIndexPairs];
		for (int i = 0; i < numIndexPairs; i++) {
			long[] key = new long[] { 0, (byte) (0xFF & (i >> 8)),
					(byte) (0xFF & i), 0 };
			IndexPair pair = new IndexPair(key, i);
			indexPairs[i] = pair;
		}
		Arrays.sort(indexPairs, IndexPair.COMPARATOR);
		index.bulkLoad(indexPairs);
		IndexPair newPair = new IndexPair(new long[] { 0, 0, 100, 1 }, 1234);
		index.insertElement(newPair);

		// Check all the values.
		for (IndexPair indexPair : indexPairs) {
			int retrievedElement = index
					.getElement(indexPair.getBitSignature());
			Assert.assertEquals(indexPair.getElementId(), retrievedElement);
		}
		Assert.assertEquals(newPair.getElementId(),
				index.getElement(newPair.getBitSignature()));
	}

	@Test
	public void testInsertSingleElementInTheFront() throws IOException {
		// Test adding a single element that should not require block splitting.
		int blockSize = 100;
		int keySize = 4;
		int numIndexPairs = 300;
		BlockBasedIndex index = new BlockBasedIndex(tempFolder, keySize,
				blockSize);

		IndexPair[] indexPairs = new IndexPair[numIndexPairs];
		for (int i = 0; i < numIndexPairs; i++) {
			long[] key = new long[] { 0, (byte) (0xFF & (i >> 8)),
					(byte) (0xFF & i), 1 };
			IndexPair pair = new IndexPair(key, i);
			indexPairs[i] = pair;
		}
		Arrays.sort(indexPairs, IndexPair.COMPARATOR);
		index.bulkLoad(indexPairs);
		IndexPair newPair = new IndexPair(new long[] { 0, 0, 0, 0 }, 1234);
		index.insertElement(newPair);

		// Check all the values.
		for (IndexPair indexPair : indexPairs) {
			int retrievedElement = index
					.getElement(indexPair.getBitSignature());
			Assert.assertEquals(indexPair.getElementId(), retrievedElement);
		}
		Assert.assertEquals(newPair.getElementId(),
				index.getElement(newPair.getBitSignature()));
	}

	@Test
	public void testInsertMultipleElements() throws IOException {
		// Test adding a large number of elements, so that block splitting will
		// need to take place.
		int blockSize = 100;
		int keySize = 4;
		int numIndexPairs = 300;
		BlockBasedIndex index = new BlockBasedIndex(tempFolder, keySize,
				blockSize);

		IndexPair[] indexPairs = new IndexPair[numIndexPairs];
		for (int i = 0; i < numIndexPairs; i++) {
			long[] key = new long[] { 0, (byte) (0xFF & (i >> 8)),
					(byte) (0xFF & i), 1 };
			IndexPair pair = new IndexPair(key, i);
			indexPairs[i] = pair;
		}
		Arrays.sort(indexPairs, IndexPair.COMPARATOR);
		index.bulkLoad(indexPairs);

		// Add the new elements.
		IndexPair[] newIndexPairs = new IndexPair[numIndexPairs];
		for (int i = 0; i < numIndexPairs; i++) {
			long[] key = new long[] { 0, (byte) (0xFF & (i >> 8)),
					(byte) (0xFF & i), 0 };
			IndexPair pair = new IndexPair(key, i);
			newIndexPairs[i] = pair;
			index.insertElement(pair);
		}

		// Check all the values.
		for (IndexPair indexPair : indexPairs) {
			int retrievedElement = index
					.getElement(indexPair.getBitSignature());
			Assert.assertEquals(indexPair.getElementId(), retrievedElement);
		}
		for (IndexPair indexPair : newIndexPairs) {
			int retrievedElement = index
					.getElement(indexPair.getBitSignature());
			Assert.assertEquals(indexPair.getElementId(), retrievedElement);
		}
	}

	@Test
	public void testDeleteElement() {
		int blockSize = 100;
		int keySize = 4;
		int numIndexPairs = 300;
		BlockBasedIndex index = new BlockBasedIndex(tempFolder, keySize,
				blockSize);

		IndexPair[] indexPairs = new IndexPair[numIndexPairs];
		for (int i = 0; i < numIndexPairs; i++) {
			byte signatureByte0 = (byte) (i % 100);
			byte signatureByte1 = (byte) ((i / 100) % 100);
			long[] key = new long[] { 0, 0, signatureByte1, signatureByte0 };
			IndexPair pair = new IndexPair(key, i);
			indexPairs[i] = pair;
		}
		Arrays.sort(indexPairs, IndexPair.COMPARATOR);
		index.bulkLoad(indexPairs);

		int indexToRemove = 4;
		IndexPair pairToRemove = indexPairs[indexToRemove];
		index.deleteElement(pairToRemove.getBitSignature());
		for (int i = 0; i < numIndexPairs; i++) {
			if (i == indexToRemove)
				continue;
			Assert.assertEquals(indexPairs[i].getElementId(),
					index.getElement(indexPairs[i].getBitSignature()));
		}

		try {
			index.getElement(pairToRemove.getBitSignature());
			Assert.fail("Element should not be included any more.");
		} catch (IllegalArgumentException e) {
			// happy path
		}
	}

	@Test
	public void testGetNearestNeighbors() {
		int blockSize = 10;
		int keySize = 4;
		int numIndexPairs = 120;
		int searchIndex = 50;
		int beamSize = 20;
		BlockBasedIndex index = new BlockBasedIndex(tempFolder, keySize,
				blockSize);

		IndexPair[] indexPairs = new IndexPair[numIndexPairs];
		for (int i = 0; i < numIndexPairs; i++) {
			long[] key = new long[] { 0, (byte) (0xFF & (i >> 8)),
					(byte) (0xFF & i), 0 };
			IndexPair pair = new IndexPair(key, i);
			indexPairs[i] = pair;
		}
		Arrays.sort(indexPairs, IndexPair.COMPARATOR);
		index.bulkLoad(indexPairs);

		IndexPair[] nearestNeighbours = index.getNearestNeighbours(new long[] {
				0, (byte) (0xFF & (searchIndex >> 8)),
				(byte) (0xFF & searchIndex), 0 }, beamSize);
		Set<IndexPair> expectedNeighbours = new HashSet<IndexPair>();
		for (int i = 0; i < 2 * beamSize; i++) {
			expectedNeighbours.add(indexPairs[searchIndex - beamSize + i]);
		}
		for (IndexPair neighbour : nearestNeighbours) {
			String msg = String.format("Unexpected neighbour: %s", neighbour);
			Assert.assertTrue(msg, expectedNeighbours.remove(neighbour));
		}
		Assert.assertTrue("Elements not discovered: " + expectedNeighbours,
				expectedNeighbours.isEmpty());
	}

	@Test
	public void testGetNearestNeighborsFromTheFront() {
		int blockSize = 10;
		int keySize = 4;
		int numIndexPairs = 120;
		int beamSize = 20;
		BlockBasedIndex index = new BlockBasedIndex(tempFolder, keySize,
				blockSize);

		IndexPair[] indexPairs = new IndexPair[numIndexPairs];
		for (int i = 0; i < numIndexPairs; i++) {
			long[] key = new long[] { 0, (byte) (0xFF & (i >> 8)),
					(byte) (0xFF & i), 1 };
			IndexPair pair = new IndexPair(key, i);
			indexPairs[i] = pair;
		}
		Arrays.sort(indexPairs, IndexPair.COMPARATOR);
		index.bulkLoad(indexPairs);

		IndexPair[] nearestNeighbours = index.getNearestNeighbours(new long[] {
				0, 0, 0, 0 }, beamSize);
		Set<IndexPair> expectedNeighbours = new HashSet<IndexPair>();
		for (int i = 0; i < beamSize; i++) {
			expectedNeighbours.add(indexPairs[i]);
		}
		for (IndexPair neighbour : nearestNeighbours) {
			String msg = String.format("Unexpected neighbour: %s", neighbour);
			Assert.assertTrue(msg, expectedNeighbours.remove(neighbour));
		}
		Assert.assertTrue("Elements not discovered: " + expectedNeighbours,
				expectedNeighbours.isEmpty());
	}

	@Test
	public void testGetNearestNeighborsFromTheEnd() {
		int blockSize = 10;
		int keySize = 4;
		int numIndexPairs = 120;
		int beamSize = 20;
		BlockBasedIndex index = new BlockBasedIndex(tempFolder, keySize,
				blockSize);

		IndexPair[] indexPairs = new IndexPair[numIndexPairs];
		for (byte i = 0; i < numIndexPairs; i++) {
			long[] key = new long[] { i, i, i, i };
			IndexPair pair = new IndexPair(key, i);
			indexPairs[i] = pair;
		}
		Arrays.sort(indexPairs, IndexPair.COMPARATOR);
		index.bulkLoad(indexPairs);

		IndexPair[] nearestNeighbours = index.getNearestNeighbours(new long[] {
				-128, -128, -128, -128 }, beamSize);
		Set<IndexPair> expectedNeighbours = new HashSet<IndexPair>();
		for (int i = indexPairs.length - beamSize; i < indexPairs.length; i++) {
			expectedNeighbours.add(indexPairs[i]);
		}
		for (IndexPair neighbour : nearestNeighbours) {
			String msg = String.format("Unexpected neighbour: %s", neighbour);
			Assert.assertTrue(msg, expectedNeighbours.remove(neighbour));
		}
		Assert.assertTrue("Elements not discovered: " + expectedNeighbours,
				expectedNeighbours.isEmpty());	
	}
	
	@AfterClass
  public static void cleanUp() {
    FileUtils.clearAndDeleteDirecotry(tempFolder);
  }
}
