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
package de.unipotsdam.hpi.storage;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.unipotsdam.hpi.indexing.IndexPair;
import de.unipotsdam.hpi.util.BitSignatureUtil;
import de.unipotsdam.hpi.util.FileUtils;
import de.unipotsdam.hpi.util.TestSettings;

public class BitSignatureDiskStorageTest {

	private static final String TEMP_FOLDER_NAME = BitSignatureDiskStorageTest.class.getName(); 
	private static Path tempFolder;
	
	@BeforeClass
	public static void setUp() throws IOException {
	  Path globalTempFolder = FileSystems.getDefault().getPath(
        TestSettings.INDEX_TMP_FOLDER);
    tempFolder = globalTempFolder.resolve(TEMP_FOLDER_NAME);
    FileUtils.createDirectoryIfNotExists(tempFolder);
	}
	
	@Test
	public void testBitSignatureStorage() throws IOException {
		Path filePath = tempFolder.resolve("testBitSignatureStorage");
		
		BitSignatureStorage storage = new BitSignatureDiskStorage(filePath, 4 * BitSignatureUtil.BASE_TYPE_SIZE, false);
		
		IndexPair[] indexPairs = new IndexPair[3];
		for (byte i = 0; i < indexPairs.length; i++) {
			long[] signature = new long[]{ i, i, i, i };
			IndexPair indexPair = new IndexPair(signature, i);
			indexPairs[i] = indexPair;
			storage.store(indexPair);
		}
		
		int i = 0;
		for (IndexPair pair : storage) {
			Assert.assertEquals(indexPairs[i], pair);
			i++;
		}
		
		Assert.assertEquals(3, i);
		
	}
	
	@AfterClass
  public static void cleanUp() {
    FileUtils.clearAndDeleteDirecotry(tempFolder);
  }
}
