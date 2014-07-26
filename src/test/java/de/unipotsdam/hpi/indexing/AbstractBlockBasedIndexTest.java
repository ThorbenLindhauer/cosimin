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

public abstract class AbstractBlockBasedIndexTest {
  
  private static final Logger logger = Logger.getLogger(AbstractBlockBasedIndexTest.class.getName());
  
  protected static final String TMP_FOLDER = SignatureStoringBlockBasedIndexTest.class
      .getName();
  protected static Path tempFolder;

  protected abstract AbstractBlockBasedIndex<?> createIndex(Path basePath, int keySize, int blockSize);
  
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
  public void testIndexBulkLoadingInsertsElements() {
    int blockSize = 100;
    int keySize = 4;
    int numIndexPairs = 300;
    AbstractBlockBasedIndex<?> index = createIndex(tempFolder, keySize, blockSize);

    IndexPair[] indexPairs = new IndexPair[numIndexPairs];
    for (int i = 0; i < numIndexPairs; i++) {
      byte signatureByte0 = (byte) (i % 100);
      byte signatureByte1 = (byte) ((i / 100) % 100);
      long[] key = new long[] { 0, 0, signatureByte1, signatureByte0 };
      IndexPair pair = createIndexPair(key, i);
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
    AbstractBlockBasedIndex<?> index = createIndex(tempFolder, keySize, blockSize);

    IndexPair[] indexPairs = new IndexPair[numIndexPairs];
    for (int i = 0; i < numIndexPairs; i++) {
      byte signatureByte0 = (byte) (i % 100);
      byte signatureByte1 = (byte) ((i / 100) % 100);
      long[] key = new long[] { 0, 0, signatureByte1, signatureByte0 };
      IndexPair pair = createIndexPair(key, i);
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
    AbstractBlockBasedIndex<?> index = createIndex(tempFolder, keySize, blockSize);

    IndexPair[] indexPairs = new IndexPair[numIndexPairs];
    for (int i = 0; i < numIndexPairs; i++) {
      byte signatureByte0 = (byte) (i % 100);
      byte signatureByte1 = (byte) ((i / 100) % 100);
      long[] key = new long[] { 0, 0, signatureByte1, signatureByte0 };
      IndexPair pair = createIndexPair(key, i);
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
    AbstractBlockBasedIndex<?> index = createIndex(tempFolder, keySize, blockSize);

    IndexPair[] indexPairs = new IndexPair[numIndexPairs];
    for (int i = 0; i < numIndexPairs; i++) {
      long[] key = new long[] { 0, (byte) (0xFF & (i >> 8)),
          (byte) (0xFF & i), 0 };
      IndexPair pair = createIndexPair(key, i);
      indexPairs[i] = pair;
    }
    Arrays.sort(indexPairs, IndexPair.COMPARATOR);
    index.bulkLoad(indexPairs);
    IndexPair newPair = createIndexPair(new long[] { 0, 0, 100, 1 }, 1234);
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
    AbstractBlockBasedIndex<?> index = createIndex(tempFolder, keySize, blockSize);

    IndexPair[] indexPairs = new IndexPair[numIndexPairs];
    for (int i = 0; i < numIndexPairs; i++) {
      long[] key = new long[] { 0, (byte) (0xFF & (i >> 8)),
          (byte) (0xFF & i), 1 };
      IndexPair pair = createIndexPair(key, i);
      indexPairs[i] = pair;
    }
    Arrays.sort(indexPairs, IndexPair.COMPARATOR);
    index.bulkLoad(indexPairs);
    IndexPair newPair = createIndexPair(new long[] { 0, 0, 0, 0 }, 1234);
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
    AbstractBlockBasedIndex<?> index = createIndex(tempFolder, keySize, blockSize);

    IndexPair[] indexPairs = new IndexPair[numIndexPairs];
    
    int idCounter = 0;
    for (int i = 0; i < numIndexPairs; i++) {
      long[] key = new long[] { 0, (byte) (0xFF & (i >> 8)),
          (byte) (0xFF & i), 1 };
      IndexPair pair = createIndexPair(key, idCounter++);
      indexPairs[i] = pair;
    }
    Arrays.sort(indexPairs, IndexPair.COMPARATOR);
    index.bulkLoad(indexPairs);

    // Add the new elements.
    IndexPair[] newIndexPairs = new IndexPair[numIndexPairs];
    for (int i = 0; i < numIndexPairs; i++) {
      long[] key = new long[] { 0, (byte) (0xFF & (i >> 8)),
          (byte) (0xFF & i), 0 };
      IndexPair pair = createIndexPair(key, idCounter++);
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
    AbstractBlockBasedIndex<?> index = createIndex(tempFolder, keySize, blockSize);

    IndexPair[] indexPairs = new IndexPair[numIndexPairs];
    for (int i = 0; i < numIndexPairs; i++) {
      byte signatureByte0 = (byte) (i % 100);
      byte signatureByte1 = (byte) ((i / 100) % 100);
      long[] key = new long[] { 0, 0, signatureByte1, signatureByte0 };
      IndexPair pair = createIndexPair(key, i);
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
  
  protected abstract IndexPair createIndexPair(long[] signature, int elementId);

  
  @AfterClass
  public static void cleanUp() {
    FileUtils.clearAndDeleteDirecotry(tempFolder);
  }
}
