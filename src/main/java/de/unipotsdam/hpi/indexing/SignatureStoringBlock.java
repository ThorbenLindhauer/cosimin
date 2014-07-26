package de.unipotsdam.hpi.indexing;

public interface SignatureStoringBlock extends LinkedBlock<SignatureStoringBlock> {

  /**
   * Loads <code>length</code> of the given pairs into the block beginning
   * from <code>startIndex</code>. The pairs are assumed to be sorted.
   */
  void bulkLoad(IndexPair[] pairs, int startIndex, int length);
  
  /**
   * Loads all the given pairs into the block.
   */
  void bulkLoad(IndexPair[] pairs);

  /**
   * Inserts a single element into the block if there is capacity left.
   */
  void insertElement(IndexPair pair);

  /**
   * Returns the elements of the block.
   */
  IndexPair[] getElements();

  /**
   * Returns a subset of the elements of this block-
   */
  IndexPair[] getElements(int startIndex, int numElements);
  
  /**
   * Tries to retrieve the ID of an element associated with the given key.
   */
  int get(long[] key);

  /**
   * Deletes all elements that are associated with the given key.
   */
  void deleteElement(long[] key);
}
