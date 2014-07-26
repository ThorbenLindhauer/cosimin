package de.unipotsdam.hpi.indexing;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import de.unipotsdam.hpi.storage.AggregatedReferenceBlockStorage;
import de.unipotsdam.hpi.util.BitSignatureUtil;

public class ReferenceBlock extends AbstractLinkedBlock<ReferenceBlock> {

  private static final long serialVersionUID = 8949680599151025769L;
  
  private static final Logger logger = Logger.getLogger(ReferenceBlock.class.getName());

  private static AtomicInteger hitCount = new AtomicInteger();
  private static AtomicInteger missCount = new AtomicInteger();
  
  private int blockId;
  
  private AggregatedReferenceBlockStorage storage;
  
  
  public ReferenceBlock(int capacity, int keySize, AggregatedReferenceBlockStorage storage, int id) {
    this.capacity = capacity;
    this.keySize = keySize;
    this.blockId = id;
    this.storage = storage;
  }
  
  public void bulkLoad(int[] elementIds, long[] startKey) {
    bulkLoad(elementIds, 0, elementIds.length, startKey);
  }

  public void bulkLoad(int[] elementIds, int offset, int length, long[] startKey) {
    if (length > capacity) {
      throw new RuntimeException("Cannot write " + length
          + " elements to a block of size " + capacity);
    }
    if (length == 0) {
      return;
    }
    
    int[] localElementIds = new int[length];
    System.arraycopy(elementIds, offset, localElementIds, 0, length);
    storage.writeBlock(blockId, localElementIds);

    if (startKey != null) {
      this.startKey = startKey;
    }
    
    size = length;
  }

  public void insertElement(int elementId, long[] key, int index) {
    if (size >= capacity)
      throw new IllegalStateException("Block is full!");
    
    int[] oldPairs = getElements();
    int[] newPairs = new int[oldPairs.length + 1];
    
    int beforeElements = Math.min(oldPairs.length, index);
    int afterElements = Math.max(0, oldPairs.length - index);
    
    System.arraycopy(oldPairs, 0, newPairs, 0, beforeElements);
    newPairs[index] = elementId;
    System.arraycopy(oldPairs, beforeElements, newPairs, index + 1, afterElements);

    if (BitSignatureUtil.COMPARATOR.compare(key, startKey) < 0) {
      startKey = key;
    }
    bulkLoad(newPairs, null);
  }

  public int[] getElements() {
    return getOrLoadElementIds();
//    return getElements(0, size);
  }

  public IndexPair[] getElements(int startIndex, int numElements) {
    throw new UnsupportedOperationException("this block type does not support ordered element lookup");
  }

  public int get(long[] key) {
    throw new UnsupportedOperationException("not possible when elements are unpermuted");
  }

  public void deleteElement(int elementId) {
    if (size == 0)
      return;

    int[] elementIds = getOrLoadElementIds();
    IntList retainedElements = new IntArrayList();
    for (int containedElementId : elementIds) {
      if (containedElementId != elementId) {
        retainedElements.add(containedElementId);
      }
    }

    // note: start key remains the same, even if the lowest element is deleted
    bulkLoad(retainedElements.toIntArray(), null);

  }

  public void close() throws IOException {

  }

  public void recover() {

  }
  
  private synchronized int[] getOrLoadElementIds() {
    return storage.getBlock(blockId);
  }
  
  public static void printStatistics() {
    int hits = hitCount.get();
    int misses = missCount.get();
    
    double accesses = hits + misses;
    logger.info(String.format("Hit rate:  %3.3f (%4d)\n", hits / accesses, hits));
  }

}
