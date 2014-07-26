/*
 * Copyright 2014 Sebastian Kruse, Thorben Lindhauer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
   * Gets the nearest neighbor around the given key. Fetches at least as many smaller
   * and greater elements as specified by the beam radius. If there are many
   * elements in the index that match the given key, the middle of the beam is
   * set arbitrarily in between them.
   * 
   * @return an array of greater size than 2 * beamRadius; size may be implementation-specific
   */
	IndexPair[] getNearestNeighboursPairs(long[] key, int beamRadius);

	/**
	 * Gets the nearest neighbor around the given key. Fetches at least as many smaller
	 * and greater elements as specified by the beam radius. If there are many
	 * elements in the index that match the given key, the middle of the beam is
	 * set arbitrarily in between them.
   * 
   * @return an array of greater size than 2 * beamRadius; size may be implementation-specific
	 */
	int[] getNearestNeighboursElementIds(long[] key, int beamRadius);

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
