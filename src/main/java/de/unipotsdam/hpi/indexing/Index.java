package de.unipotsdam.hpi.indexing;

import java.io.IOException;
import java.io.Serializable;

/**
 * Keys are bit signatures (ie byte arrays). Values are integers that represent
 * the id of the articles.
 * 
 * @author Thorben
 * 
 */
public interface Index extends Serializable {

	void insertElement(long[] key, int value) throws IOException;

	void insertElement(IndexPair pair) throws IOException;
	
	void deleteElement(long[] key);

	/**
	 * Gets the nearest neighbor around the given key. Fetches as many smaller
	 * and greater elements as specified by the beam radius. If there are many
	 * elements in the index that match the given key, the middle of the beam is
	 * set arbitrarily in between them.
	 */
	IndexPair[] getNearestNeighbours(long[] key, int beamRadius);

	void bulkLoad(IndexPair[] keyValuePairs);

	/**
	 * Returns the value associated with the element or throws an
	 * IllegalArgumentException if not present.
	 */
	int getElement(long[] key) throws IllegalArgumentException;

	/**
	 * Closes connections to any system resources used by this index.
	 */
	void close() throws IOException;
	
	/**
	 * Should be called after an index is deserialized.
	 */
	void recover();
}
