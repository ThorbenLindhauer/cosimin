package de.unipotsdam.hpi.storage;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.unipotsdam.hpi.input.InputVector;
import de.unipotsdam.hpi.input.SparseInputVector;
import de.unipotsdam.hpi.sparse.DefaultSparseIntList;
import de.unipotsdam.hpi.sparse.SparseIntList;
import de.unipotsdam.hpi.util.FileUtils;
import de.unipotsdam.hpi.util.TestSettings;

public class SparseInputVectorStorageTest {

  private static final String TEMP_FOLDER_NAME = SparseInputVectorStorageTest.class.getName(); 
  private static Path tempFolder;
  
  @BeforeClass
  public static void setUp() throws IOException {
    Path globalTempFolder = FileSystems.getDefault().getPath(
        TestSettings.INDEX_TMP_FOLDER);
    tempFolder = globalTempFolder.resolve(TEMP_FOLDER_NAME);
    FileUtils.createDirectoryIfNotExists(tempFolder);
  }
  
  @Test
  public void testStorage() {
    Path filePath = tempFolder.resolve("testStorage");
    int numVectors = 1;
    
    SparseInputVectorStorage storage = new SparseInputVectorStorage(filePath);
    
    List<SparseInputVector> testVectors = new ArrayList<SparseInputVector>();
    for (int i = 0; i < numVectors; i++) {
      DefaultSparseIntList backingIntList = new DefaultSparseIntList(i);
      for (int j = 0; j < i + 1; j++) {
        backingIntList.add(j, j + 1);
      }
      
      SparseInputVector vector = new SparseInputVector(i, backingIntList);
      storage.store(vector);
      testVectors.add(vector);
    }
    
    storage.closeOutput();
    
    Iterator<InputVector> iterator = storage.createFileIterator();
    List<InputVector> recoveredVectors = new ArrayList<InputVector>();
    
    while (iterator.hasNext()) {
      InputVector readInputVector = iterator.next();
      recoveredVectors.add(readInputVector);
    }
    
    for (int i = 0; i < numVectors; i++) {
      SparseInputVector testVector = testVectors.get(i);
      InputVector recoveredVector = recoveredVectors.get(i);
      Assert.assertEquals(testVector.getId(), recoveredVector.getId());
      
      SparseIntList testIntList = testVector.toSparseIntList();
      SparseIntList recoveredIntList = recoveredVector.toSparseIntList();
      Assert.assertEquals(testIntList.size(), recoveredIntList.size());
      Assert.assertEquals(testIntList, recoveredIntList);
    }
    
  }
  
  @AfterClass
  public static void cleanUp() {
    FileUtils.clearAndDeleteDirecotry(tempFolder);
  }
}
