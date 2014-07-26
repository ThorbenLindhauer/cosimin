package de.unipotsdam.hpi.indexing;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;

import de.unipotsdam.hpi.storage.AggregatedReferenceBlockStorage;
import de.unipotsdam.hpi.storage.BitSignatureIndex;
import de.unipotsdam.hpi.util.FileUtils;
import de.unipotsdam.hpi.util.TestSettings;

public class ReferenceBlockTest extends AbstractIndexBlockTest<ReferenceBlock> {

  private static final Logger logger = Logger.getLogger(ReferenceBlockTest.class.getName());
  
  private static final String TMP_FOLDER = ReferenceBlockTest.class.getName();
  
  private BitSignatureIndex bitSignatureIndex;
  
  private int blockIdCounter;
  
  @BeforeClass
  public static void createTempDirectory() throws IOException {
    Path globalTempFolder = FileSystems.getDefault().getPath(
        TestSettings.INDEX_TMP_FOLDER);
    tempFolder = globalTempFolder.resolve(TMP_FOLDER);
    FileUtils.createDirectoryIfNotExists(globalTempFolder);
    FileUtils.createDirectoryIfNotExists(tempFolder);

    logger.info("Using temporary folder " + tempFolder);
  }
  
  @Before
  public void setUpBitsignatureIndex() {
    bitSignatureIndex = new BitSignatureIndex();
    blockIdCounter = 0;
  }
  
  @Override
  protected ReferenceBlock newBlock(int capacity, int keySize, Path filePath)
      throws IOException {
    return new ReferenceBlock(capacity, keySize, new AggregatedReferenceBlockStorage(filePath), blockIdCounter++);
  }

  @Override
  protected void bulkLoad(ReferenceBlock block, IndexPair[] pairs) {
    int[] elementIds = new int[pairs.length];
    
    for (int i = 0; i < pairs.length; i++) {
      IndexPair pair = pairs[i];
      bitSignatureIndex.add(pair);
      
      elementIds[i] = pair.getElementId();
    }
    
    long[] startKey = null;
    
    if (pairs.length > 0) {
      startKey = pairs[0].getBitSignature();
    }
    
    block.bulkLoad(elementIds, startKey);
  }

  @Override
  protected void insertElement(ReferenceBlock block, IndexPair pair) {
    block.insertElement(pair.getElementId(), pair.getBitSignature(), 0);
    
  }

  @Override
  protected IndexPair[] getBlockElements(ReferenceBlock block, int offset,
      int length) {
    int[] elementIds = block.getElements();
    
    IndexPair[] indexPairs = new IndexPair[elementIds.length];
    
    for (int i = 0; i < elementIds.length; i++) {
      IndexPair pair = bitSignatureIndex.getIndexPair(elementIds[i]);
      indexPairs[i] = pair;
    }
    
    return indexPairs;
  }
  
  // TODO test insertion
  
  // TODO test deletion

}
