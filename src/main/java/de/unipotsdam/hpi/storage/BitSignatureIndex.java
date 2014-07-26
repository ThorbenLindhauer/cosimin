package de.unipotsdam.hpi.storage;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import de.unipotsdam.hpi.indexing.IndexPair;

public class BitSignatureIndex {

  private Int2ObjectMap<IndexPair> index = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<IndexPair>());
  
  public void add(IndexPair indexPair) {
    index.put(indexPair.getElementId(), indexPair);
  }
  
  public IndexPair getIndexPair(int id) {
    return index.get(id);
  }
  
  public boolean contains(int elementId) {
    return index.containsKey(elementId);
  }
}
