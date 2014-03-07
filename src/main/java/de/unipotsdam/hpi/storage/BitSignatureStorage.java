package de.unipotsdam.hpi.storage;

import de.unipotsdam.hpi.database.VectorDatabase;
import de.unipotsdam.hpi.indexing.Index;
import de.unipotsdam.hpi.indexing.IndexPair;

/**
 * Stores bit signatures ({@link IndexPair}) the import process of a {@link VectorDatabase},
 * before they are written into {@link Index}es.
 * 
 * @author Sebastian
 * 
 */
public interface BitSignatureStorage extends Iterable<IndexPair> {

	/**
	 * Storing must be thread-safe.
	 */
	void store(IndexPair indexPair);

	public void clear();

	public void flushOutput();

	public void closeOutput();
}
