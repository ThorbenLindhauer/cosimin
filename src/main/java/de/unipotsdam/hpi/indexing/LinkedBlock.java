package de.unipotsdam.hpi.indexing;

import java.io.IOException;
import java.io.Serializable;

/**
 * A linked block is used by a {@link BlockBasedIndex} to store a set of
 * {@link IndexPair}s. Blocks are organized in a double-linked list and have a
 * key to support vector queries without having to look into the actually
 * contained elements.
 * 
 * @author Sebastian
 * 
 */
public interface LinkedBlock extends Serializable {

	/**
	 * Loads <code>length</code> of the given pairs into the block beginning
	 * from <code>startIndex</code>. The pairs are assumed to be sorted.
	 */
	void bulkLoad(IndexPair[] pairs, int startIndex, int length);

	/**
	 * Inserts a single element into the block if there is capacity left.
	 */
	void insertElement(IndexPair pair);

	/**
	 * Returns the number of elements in this block.
	 */
	int getSize();

	/**
	 * Returns the maximum number of elements allowed in this block.
	 */
	int getCapacity();

	/**
	 * Links the block.
	 */
	void setPreviousBlock(LinkedBlock block);

	/**
	 * Links the block.
	 */
	void setNextBlock(LinkedBlock newBlock);

	/**
	 * Returns the previous block.
	 */
	LinkedBlock getPreviousBlock();

	/**
	 * Returns the next block.
	 */
	LinkedBlock getNextBlock();

	/**
	 * Returns the elements of the block.
	 */
	IndexPair[] getElements();

	/**
	 * Returns a subset of the elements of this block-
	 */
	IndexPair[] getElements(int startIndex, int numElements);

	/**
	 * Returns the key of the first contained element in this block.
	 */
	long[] getStartKey();

	/**
	 * Tries to retrieve the ID of an element associated with the given key.
	 */
	int get(long[] key);

	/**
	 * Deletes all elements that are associated with the given key.
	 */
	void deleteElement(long[] key);

	/**
	 * Closes connection to any system resources held by this block.
	 */
	void close() throws IOException;

	/**
	 * Is called when the containing index is recovered.
	 */
	void recover();

}
