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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.unipotsdam.hpi.indexing.IndexPair;

public class BitSignatureInMemoryStorage implements BitSignatureStorage {

	private List<IndexPair> signatures = new ArrayList<IndexPair>();
	
	public Iterator<IndexPair> iterator() {
		return signatures.iterator();
	}

	synchronized public void store(IndexPair indexPair) {
		signatures.add(indexPair);
	}

	public void clear() {
		signatures.clear();
	}

	public void flushOutput() {
	}

	public void closeOutput() {
	}

  public BitSignatureIndex generateIndex() {
    BitSignatureIndex index = new BitSignatureIndex();
    for (IndexPair pair : signatures) {
      index.add(pair);
    }
    return index;
  }

}
