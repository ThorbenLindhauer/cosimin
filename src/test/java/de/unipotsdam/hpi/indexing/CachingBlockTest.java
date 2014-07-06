/*
 * Copyright 2014 Sebastian Kruse, Thorben Lindhauer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

public class CachingBlockTest extends AbstractIndexBlockTest {

  private static final Logger logger = Logger.getLogger(CachingBlockTest.class.getName());
  
	private static final String TMP_FOLDER = CachingBlockTest.class.getName();

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
	public void testBlockBulkLoadingWithoutCache() throws IOException {
		int numElements = 10;
		int keySize = 4;
		Path filePath = tempFolder.resolve("BlockTest.testBlockBulkLoading");
		CachingBlock block = new CachingBlock(numElements, keySize, filePath);
		
		IndexPair[] indexPairs = new IndexPair[numElements];
		for (int i = 0; i < numElements; i++) {
			long[] key = new long[] { i, i, i, i };
			int elementId = (int) i;
			IndexPair pair = new IndexPair(key, elementId);
			indexPairs[i] = pair;
		}
		
		block.bulkLoad(indexPairs);
		block.clearCache();
		
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
	public void testGetElementWithoutCache() throws IOException {
		int numElements = 10;
		int keySize = 4;
		Path filePath = tempFolder.resolve("BlockTest.testGetElement");
		CachingBlock block = new CachingBlock(numElements, keySize, filePath);
		
		IndexPair[] indexPairs = new IndexPair[numElements];
		for (int i = 0; i < numElements; i++) {
			long[] key = new long[] { i, i, i, i };
			int elementId = (int) i;
			IndexPair pair = new IndexPair(key, elementId);
			indexPairs[i] = pair;
		}
		
		block.bulkLoad(indexPairs);
		block.clearCache();
		
		for (IndexPair pair : indexPairs) {
			int retrievedId = block.get(pair.getBitSignature());
			Assert.assertEquals(pair.getElementId(), retrievedId);
		}
	}

	@Test
	public void testGetNonExistingElementWithoutCache() throws IOException {
		int numElements = 10;
		int keySize = 4;
		Path filePath = tempFolder
				.resolve("BlockTest.testGetNonExistingElement");
		CachingBlock block = new CachingBlock(numElements, keySize, filePath);
		
		IndexPair[] indexPairs = new IndexPair[numElements];
		for (int i = 0; i < numElements; i++) {
			long[] key = new long[] { i, i, i, i };
			int elementId = (int) i;
			IndexPair pair = new IndexPair(key, elementId);
			indexPairs[i] = pair;
		}
		
		block.bulkLoad(indexPairs);
		block.clearCache();
		
		try {
			block.get(new long[] { 3, 2, 1, 0 });
			Assert.fail("Expected exception.");
		} catch (IllegalArgumentException e) {
			// happy path
		}
	}

	@Test
	public void testInsertElementWithoutCache() throws IOException {
		int numElements = 10;
		int keySize = 4;
		Path filePath = tempFolder.resolve("BlockTest.testInsertElement");
		CachingBlock block = new CachingBlock(numElements + 1, keySize, filePath);
		
		IndexPair[] indexPairs = new IndexPair[numElements];
		for (int i = 0; i < numElements; i++) {
			long[] key = new long[] { i, i, i, i };
			int elementId = (int) i;
			IndexPair pair = new IndexPair(key, elementId);
			indexPairs[i] = pair;
		}
		
		block.bulkLoad(indexPairs);
		block.clearCache();
		
		IndexPair newPair = new IndexPair(new long[] { 2, 1, 0, 1 }, 2101);
		block.insertElement(newPair);
		block.clearCache();
		
		for (int i = 0; i < numElements; i++) {
			Assert.assertEquals(indexPairs[i].getElementId(),
					block.get(indexPairs[i].getBitSignature()));
		}
		Assert.assertEquals(newPair.getElementId(),
				block.get(newPair.getBitSignature()));
	}
	
	@Test
	public void testInsertElementIntoFullBlockWithoutCache() throws IOException {
		int numElements = 10;
		int keySize = 4;
		Path filePath = tempFolder.resolve("BlockTest.testInsertElementIntoFullBlock");
		CachingBlock block = new CachingBlock(numElements, keySize, filePath);
		
		IndexPair[] indexPairs = new IndexPair[numElements];
		for (int i = 0; i < numElements; i++) {
			long[] key = new long[] { i, i, i, i };
			int elementId = (int) i;
			IndexPair pair = new IndexPair(key, elementId);
			indexPairs[i] = pair;
		}
		block.bulkLoad(indexPairs);
		block.clearCache();
		
		IndexPair newPair = new IndexPair(new long[] { 2, 1, 0, 1 }, 2101);
		try {
			block.insertElement(newPair);
			Assert.fail("Exception expected.");
		} catch (IllegalStateException e) {
			// happy path
		}
	}

	@Test
	public void testDeleteElementWithoutCache() throws IOException {
		int numElements = 10;
		int keySize = 4;
		Path filePath = tempFolder.resolve("BlockTest.testDeleteElement");
		CachingBlock block = new CachingBlock(numElements, keySize, filePath);
		
		IndexPair[] indexPairs = new IndexPair[numElements];
		for (int i = 0; i < numElements; i++) {
			long[] key = new long[] { i, i, i, i };
			int elementId = (int) i;
			IndexPair pair = new IndexPair(key, elementId);
			indexPairs[i] = pair;
		}
		
		block.bulkLoad(indexPairs);
		block.clearCache();
		
		int indexToRemove = 4;
		IndexPair pairToRemove = indexPairs[indexToRemove];
		block.deleteElement(pairToRemove.getBitSignature());
		block.clearCache();
		
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
	
	//TODO: deleting the file afterwards is apparently not possible on Windows
  // when using file mapped byte buffers
	@AfterClass
  public static void cleanUp() {
    FileUtils.clearAndDeleteDirecotry(tempFolder);
  }

  @Override
  protected LinkedBlock newBlock(int capacity, int keySize, Path filePath)
      throws IOException {
    return new CachingBlock(capacity, keySize, filePath);
  }
	
	
}
