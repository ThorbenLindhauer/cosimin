package de.unipotsdam.hpi.indexing;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.unipotsdam.hpi.util.FileUtils;
import de.unipotsdam.hpi.util.TestSettings;

public class MappedIoBlockTest {

  private static final Logger logger = Logger.getLogger(MappedIoBlockTest.class.getName());
  
	private static final String TMP_FOLDER = MappedIoBlockTest.class.getName();

	private static Path tempFolder;

	@BeforeClass
	public static void createTempDirectory() throws IOException {
		Path globalTempFolder = FileSystems.getDefault().getPath(
				TestSettings.INDEX_TMP_FOLDER);
		tempFolder = globalTempFolder.resolve(TMP_FOLDER);
		FileUtils.createDirectoryIfNotExists(globalTempFolder);
		FileUtils.createDirectoryIfNotExists(tempFolder);

		logger.info("Using temporary folder " + tempFolder);
	}

	@Test
	public void testBlockCreation() {
		int numElements = 10;
		int keySize = 4;
		Path filePath = tempFolder.resolve("BlockTest.testBlockWriting");
		try {
			MappedIoBlock block = new MappedIoBlock(numElements, keySize, filePath);
			Assert.assertEquals(numElements, block.getCapacity());
			// happy path
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testBlockBulkLoading() throws IOException {
		int numElements = 10;
		int keySize = 4;
		Path filePath = tempFolder.resolve("BlockTest.testBlockBulkLoading");
		MappedIoBlock block = new MappedIoBlock(numElements, keySize, filePath);

		IndexPair[] indexPairs = new IndexPair[numElements];
		for (int i = 0; i < numElements; i++) {
			long[] key = new long[] { i, i, i, i };
			int elementId = (int) i;
			IndexPair pair = new IndexPair(key, elementId);
			indexPairs[i] = pair;
		}

		block.bulkLoad(indexPairs);

		IndexPair[] retrievedIndexPairs = block.getElements(0, 10);
		for (int i = 0; i < numElements; i++) {
			IndexPair pair = retrievedIndexPairs[i];
			Assert.assertTrue(Arrays.equals(indexPairs[i].getBitSignature(),
					pair.getBitSignature()));
			Assert.assertEquals(indexPairs[i].getElementId(),
					pair.getElementId());
		}
	}

	@Test
	public void testOverlongBulkLoading() throws IOException {
		int numElements = 10;
		int keySize = 4;
		Path filePath = tempFolder.resolve("BlockTest.testOverlongBulkLoading");
		MappedIoBlock block = new MappedIoBlock(numElements, keySize, filePath);
		IndexPair[] indexPairs = new IndexPair[numElements + 1];
		for (int i = 0; i < numElements; i++) {
			indexPairs[i] = new IndexPair();
		}

		try {
			block.bulkLoad(indexPairs);
			Assert.fail("Exception expected");
		} catch (RuntimeException e) {
			// happy path
		}
	}

	@Test
	public void testGetElement() throws IOException {
		int numElements = 10;
		int keySize = 4;
		Path filePath = tempFolder.resolve("BlockTest.testGetElement");
		MappedIoBlock block = new MappedIoBlock(numElements, keySize, filePath);

		IndexPair[] indexPairs = new IndexPair[numElements];
		for (int i = 0; i < numElements; i++) {
			long[] key = new long[] { i, i, i, i };
			int elementId = (int) i;
			IndexPair pair = new IndexPair(key, elementId);
			indexPairs[i] = pair;
		}

		block.bulkLoad(indexPairs);

		for (IndexPair pair : indexPairs) {
			int retrievedId = block.get(pair.getBitSignature());
			Assert.assertEquals(pair.getElementId(), retrievedId);
		}
	}

	@Test
	public void testGetNonExistingElement() throws IOException {
		int numElements = 10;
		int keySize = 4;
		Path filePath = tempFolder
				.resolve("BlockTest.testGetNonExistingElement");
		MappedIoBlock block = new MappedIoBlock(numElements, keySize, filePath);

		IndexPair[] indexPairs = new IndexPair[numElements];
		for (int i = 0; i < numElements; i++) {
			long[] key = new long[] { i, i, i, i };
			int elementId = (int) i;
			IndexPair pair = new IndexPair(key, elementId);
			indexPairs[i] = pair;
		}

		block.bulkLoad(indexPairs);

		try {
			block.get(new long[] { 3, 2, 1, 0 });
			Assert.fail("Expected exception.");
		} catch (IllegalArgumentException e) {
			// happy path
		}
	}

	@Test
	public void testInsertElement() throws IOException {
		int numElements = 10;
		int keySize = 4;
		Path filePath = tempFolder.resolve("BlockTest.testInsertElement");
		MappedIoBlock block = new MappedIoBlock(numElements + 1, keySize, filePath);

		IndexPair[] indexPairs = new IndexPair[numElements];
		for (int i = 0; i < numElements; i++) {
			long[] key = new long[] { i, i, i, i };
			int elementId = (int) i;
			IndexPair pair = new IndexPair(key, elementId);
			indexPairs[i] = pair;
		}

		block.bulkLoad(indexPairs);
		IndexPair newPair = new IndexPair(new long[] { 2, 1, 0, 1 }, 2101);
		block.insertElement(newPair);
		for (int i = 0; i < numElements; i++) {
			Assert.assertEquals(indexPairs[i].getElementId(),
					block.get(indexPairs[i].getBitSignature()));
		}
		Assert.assertEquals(newPair.getElementId(),
				block.get(newPair.getBitSignature()));
	}
	
	@Test
	public void testInsertElementIntoFullBlock() throws IOException {
		int numElements = 10;
		int keySize = 4;
		Path filePath = tempFolder.resolve("BlockTest.testInsertElementIntoFullBlock");
		MappedIoBlock block = new MappedIoBlock(numElements, keySize, filePath);
		
		IndexPair[] indexPairs = new IndexPair[numElements];
		for (int i = 0; i < numElements; i++) {
			long[] key = new long[] { i, i, i, i };
			int elementId = (int) i;
			IndexPair pair = new IndexPair(key, elementId);
			indexPairs[i] = pair;
		}
		block.bulkLoad(indexPairs);

		IndexPair newPair = new IndexPair(new long[] { 2, 1, 0, 1 }, 2101);
		try {
			block.insertElement(newPair);
			Assert.fail("Exception expected.");
		} catch (IllegalStateException e) {
			// happy path
		}
	}

	@Test
	public void testDeleteElement() throws IOException {
		int numElements = 10;
		int keySize = 4;
		Path filePath = tempFolder.resolve("BlockTest.testDeleteElement");
		MappedIoBlock block = new MappedIoBlock(numElements, keySize, filePath);

		IndexPair[] indexPairs = new IndexPair[numElements];
		for (int i = 0; i < numElements; i++) {
			long[] key = new long[] { i, i, i, i };
			int elementId = (int) i;
			IndexPair pair = new IndexPair(key, elementId);
			indexPairs[i] = pair;
		}

		block.bulkLoad(indexPairs);
		int indexToRemove = 4;
		IndexPair pairToRemove = indexPairs[indexToRemove];
		block.deleteElement(pairToRemove.getBitSignature());
		for (int i = 0; i < numElements; i++) {
			if (i == indexToRemove)
				continue;
			Assert.assertEquals(indexPairs[i].getElementId(),
					block.get(indexPairs[i].getBitSignature()));
		}

		try {
			block.get(pairToRemove.getBitSignature());
			Assert.fail("Element should not be included any more.");
		} catch (IllegalArgumentException e) {
			// happy path
		}
	}
	
	// TODO: deleting the file afterwards is apparently not possible on Windows
	// when using file mapped byte buffers
	@AfterClass
  public static void cleanUp() {
    FileUtils.clearAndDeleteDirecotry(tempFolder);
  }
}
