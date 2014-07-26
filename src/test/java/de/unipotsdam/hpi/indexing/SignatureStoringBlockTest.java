package de.unipotsdam.hpi.indexing;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

public abstract class SignatureStoringBlockTest extends AbstractIndexBlockTest<SignatureStoringBlock> {

  @Test
  public void testGetElement() throws IOException {
    int numElements = 10;
    int keySize = 4;
    Path filePath = tempFolder.resolve("BlockTest.testGetElement");
    SignatureStoringBlock block = newBlock(numElements, keySize, filePath);

    IndexPair[] indexPairs = new IndexPair[numElements];
    for (int i = 0; i < numElements; i++) {
      long[] key = new long[] { i, i, i, i };
      int elementId = (int) i;
      IndexPair pair = new IndexPair(key, elementId);
      indexPairs[i] = pair;
    }

    bulkLoad(block, indexPairs);

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
    SignatureStoringBlock block = newBlock(numElements, keySize, filePath);

    IndexPair[] indexPairs = new IndexPair[numElements];
    for (int i = 0; i < numElements; i++) {
      long[] key = new long[] { i, i, i, i };
      int elementId = (int) i;
      IndexPair pair = new IndexPair(key, elementId);
      indexPairs[i] = pair;
    }

    bulkLoad(block, indexPairs);

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
    SignatureStoringBlock block = newBlock(numElements + 1, keySize, filePath);

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
  public void testDeleteElement() throws IOException {
    int numElements = 10;
    int keySize = 4;
    Path filePath = tempFolder.resolve("BlockTest.testDeleteElement");
    SignatureStoringBlock block = newBlock(numElements, keySize, filePath);

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
}
