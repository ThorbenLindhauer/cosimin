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
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import de.unipotsdam.hpi.util.FileUtils;
import de.unipotsdam.hpi.util.TestSettings;

public class MappedIoBlockTest extends AbstractSignatureStoringBlockTest {

  private static final Logger logger = Logger.getLogger(MappedIoBlockTest.class.getName());
  
	private static final String TMP_FOLDER = MappedIoBlockTest.class.getName();

	@BeforeClass
	public static void createTempDirectory() throws IOException {
		Path globalTempFolder = FileSystems.getDefault().getPath(
				TestSettings.INDEX_TMP_FOLDER);
		tempFolder = globalTempFolder.resolve(TMP_FOLDER);
		FileUtils.createDirectoryIfNotExists(globalTempFolder);
		FileUtils.createDirectoryIfNotExists(tempFolder);

		logger.info("Using temporary folder " + tempFolder);
	}

	// TODO: deleting the file afterwards is apparently not possible on Windows
	// when using file mapped byte buffers
	@AfterClass
  public static void cleanUp() {
    FileUtils.clearAndDeleteDirecotry(tempFolder);
  }

  protected SignatureStoringBlock newBlock(int capacity, int keySize, Path filePath) throws IOException {
    return new MappedIoBlock(capacity, keySize, filePath);
  }
}
