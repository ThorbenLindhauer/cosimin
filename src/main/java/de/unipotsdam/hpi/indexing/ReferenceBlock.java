package de.unipotsdam.hpi.indexing;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import de.unipotsdam.hpi.util.BitSignatureUtil;
import de.unipotsdam.hpi.util.EncodingUtils;

public class ReferenceBlock extends AbstractLinkedBlock<ReferenceBlock> {

  private static final long serialVersionUID = 8949680599151025769L;
  
  private static final Logger logger = Logger.getLogger(ReferenceBlock.class.getName());

  private static AtomicInteger hitCount = new AtomicInteger();
  private static AtomicInteger missCount = new AtomicInteger();
  
  private File file;
  
  transient private SoftReference<int[]> cache;
  
  
  public ReferenceBlock(int capacity, int keySize, Path filePath) {
    this.capacity = capacity;
    this.keySize = keySize;
    this.file = filePath.toFile();
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
    OutputStream out = null;
    try {
      out = new BufferedOutputStream(new FileOutputStream(file));
      for (int i = offset; i < offset + length; i++) {
        int elementId = elementIds[i];
        EncodingUtils.writeInt(elementId, out);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        if (out != null)
          out.close();
      } catch (IOException e) {
        e.printStackTrace();
      }

    }

    if (startKey != null) {
      this.startKey = startKey;
    }
    
    size = length;
    
    int[] cachedElementIds = new int[length];
    System.arraycopy(elementIds, offset, cachedElementIds, 0, length);

    cache = new SoftReference<int[]>(cachedElementIds);
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
    int[] elementIds;
    if (cache != null && (elementIds = cache.get()) != null) {
      hitCount.incrementAndGet();
      return elementIds;
    }

    missCount.incrementAndGet();
    elementIds = new int[size];
    FileInputStream in = null;
    try {
      in = new FileInputStream(file);
      // in.skip(startPos);
      for (int i = 0; i < size; i++) {
        int elementId = EncodingUtils.readInt(in);
        elementIds[i] = elementId;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (in != null)
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }

    this.cache = new SoftReference<int[]>(elementIds);

    return elementIds;
  }
  
  public void clearCache() {
    cache = null;
  }
  
  public static void printStatistics() {
    int hits = hitCount.get();
    int misses = missCount.get();
    
    double accesses = hits + misses;
    logger.info(String.format("Hit rate:  %3.3f (%4d)\n", hits / accesses, hits));
  }

}
