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
	
	public BitSignatureIndex generateIndex();
}
