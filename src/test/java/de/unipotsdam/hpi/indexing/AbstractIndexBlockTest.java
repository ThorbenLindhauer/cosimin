package de.unipotsdam.hpi.indexing;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractIndexBlockTest<T extends LinkedBlock<T>> {

  protected static Path tempFolder;
  
  @Test
  public void testBlockCreation() throws IOException {
    int numElements = 10;
    int keySize = 4;
    Path filePath = tempFolder.resolve("BlockTest.testBlockWriting");
    try {
      T block = newBlock(numElements, keySize, filePath);
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
    T block = newBlock(numElements, keySize, filePath);

    IndexPair[] indexPairs = new IndexPair[numElements];
    for (int i = 0; i < numElements; i++) {
      long[] key = new long[] { i, i, i, i };
      int elementId = (int) i;
      IndexPair pair = new IndexPair(key, elementId);
      indexPairs[i] = pair;
    }

    bulkLoad(block, indexPairs);

    IndexPair[] retrievedIndexPairs = getBlockElements(block, 0, 10);
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
    T block = newBlock(numElements, keySize, filePath);
    IndexPair[] indexPairs = new IndexPair[numElements + 1];
    for (int i = 0; i < numElements; i++) {
      indexPairs[i] = new IndexPair();
    }

    try {
      bulkLoad(block, indexPairs);
      Assert.fail("Exception expected");
    } catch (RuntimeException e) {
      // happy path
    }
  }
  
  
  
  @Test
  public void testInsertElementIntoFullBlock() throws IOException {
    int numElements = 10;
    int keySize = 4;
    Path filePath = tempFolder.resolve("BlockTest.testInsertElementIntoFullBlock");
    T block = newBlock(numElements, keySize, filePath);
    
    IndexPair[] indexPairs = new IndexPair[numElements];
    for (int i = 0; i < numElements; i++) {
      long[] key = new long[] { i, i, i, i };
      int elementId = (int) i;
      IndexPair pair = new IndexPair(key, elementId);
      indexPairs[i] = pair;
    }
    bulkLoad(block, indexPairs);

    IndexPair newPair = new IndexPair(new long[] { 2, 1, 0, 1 }, 2101);
    try {
      insertElement(block, newPair);
      Assert.fail("Exception expected.");
    } catch (IllegalStateException e) {
      // happy path
    }
  }
  
  protected abstract T newBlock(int capacity, int keySize, Path filePath) throws IOException;
  
  protected abstract void bulkLoad(T block, IndexPair[] pairs);
  
  /**
   * Insert at any position
   * 
   * @param block
   * @param pair
   */
  protected abstract void insertElement(T block, IndexPair pair);
  
  protected abstract IndexPair[] getBlockElements(T block, int offset, int length);
  
}
