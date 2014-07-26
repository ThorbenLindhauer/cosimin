package de.unipotsdam.hpi.storage;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.unipotsdam.hpi.util.FileUtils;
import de.unipotsdam.hpi.util.TestSettings;

public class AggregatedReferenceBlockStorageTest {

  private static final String TEMP_FOLDER_NAME = AggregatedReferenceBlockStorageTest.class.getName(); 
  private static Path tempFolder;
  
  @BeforeClass
  public static void setUp() throws IOException {
    Path globalTempFolder = FileSystems.getDefault().getPath(
        TestSettings.INDEX_TMP_FOLDER);
    tempFolder = globalTempFolder.resolve(TEMP_FOLDER_NAME);
    FileUtils.createDirectoryIfNotExists(globalTempFolder);
    FileUtils.createDirectoryIfNotExists(tempFolder);
  }
  
  @Test
  public void testPersistingMultipleEntries() {
    Path filePath = tempFolder.resolve("AggregatedReferenceBlockStorageTest.testPersistingMultipleEntries");
    AggregatedReferenceBlockStorage storage = new AggregatedReferenceBlockStorage(filePath);
    
    int[] values1 = new int[20];
    for (int i = 0; i < 20; i++) {
      values1[i] = i;
    }
    
    int[] values2 = new int[40];
    for (int i = 0; i < 20; i++) {
      values1[i] = i + 40;
    }
    
    storage.writeBlock(1, values1);
    storage.clearCache();
    int[] loadedValues1 = storage.getBlock(1);
    Assert.assertArrayEquals(values1, loadedValues1);
    
    storage.writeBlock(2, values2);
    storage.clearCache();
    loadedValues1 = storage.getBlock(1);
    int[] loadedValues2 = storage.getBlock(2);
    
    Assert.assertArrayEquals(values1, loadedValues1);
    Assert.assertArrayEquals(values2, loadedValues2);
  }
  
  @AfterClass
  public static void cleanUp() {
    FileUtils.clearAndDeleteDirecotry(tempFolder);
  }
}
