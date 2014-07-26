package de.unipotsdam.hpi.storage;

import it.unimi.dsi.fastutil.ints.Int2IntAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import de.unipotsdam.hpi.util.EncodingUtils;

public class AggregatedReferenceBlockStorage implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private File file;
  transient private SoftReference<Int2ObjectMap<int[]>> cache;
  private Int2IntMap cacheMetaData;
  
  private static AtomicInteger hitCount = new AtomicInteger();
  private static AtomicInteger missCount = new AtomicInteger();
  
  public AggregatedReferenceBlockStorage(Path filePath) {
    this.file = filePath.toFile();
    this.cacheMetaData = new Int2IntAVLTreeMap();
  }
  
  public void writeBlock(int id, int[] values) {
    cacheMetaData.put(id, values.length);
    
    Int2ObjectMap<int[]> storage = getOrLoadStorage();
    storage.put(id, values);
    
    OutputStream out = null;
    try {
      out = new BufferedOutputStream(new FileOutputStream(file));
      
      for (Entry metaEntry : cacheMetaData.int2IntEntrySet()) {
        int[] storedValues = storage.get(metaEntry.getIntKey());
        for (int i = 0; i < storedValues.length; i++) {
          EncodingUtils.writeInt(storedValues[i], out);
        }
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
  }
  
  public int[] getBlock(int id) {
    Int2ObjectMap<int[]> storage = getOrLoadStorage();
    
    return storage.get(id);
  }
  
  private synchronized Int2ObjectMap<int[]> getOrLoadStorage() {
    Int2ObjectMap<int[]> cachedElements;
    if (cache != null && (cachedElements = cache.get()) != null) {
      hitCount.incrementAndGet();
      return cachedElements;
    }

    missCount.incrementAndGet();
    cachedElements = new Int2ObjectOpenHashMap<int[]>();
    FileInputStream in = null;
    try {
      file.createNewFile();
      in = new FileInputStream(file);
      // in.skip(startPos);
      
      for (Entry metaEntry : cacheMetaData.int2IntEntrySet()) {
        int keyId = metaEntry.getIntKey();
        int valueSize = metaEntry.getIntValue();
        
        int[] values = new int[valueSize];
        
        for (int i = 0; i < valueSize; i++) {
          int elementId = EncodingUtils.readInt(in);
          values[i] = elementId;
        }
        
        cachedElements.put(keyId, values);
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

    this.cache = new SoftReference<Int2ObjectMap<int[]>>(cachedElements);

    return cachedElements;
  }
  
  public void clearCache() {
    cache = null;
  }
  
}
