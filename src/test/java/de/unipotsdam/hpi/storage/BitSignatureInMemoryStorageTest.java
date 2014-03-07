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

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.unipotsdam.hpi.indexing.IndexPair;

public class BitSignatureInMemoryStorageTest {

	@Test
	public void testBitSignatureStorage() throws IOException {
		BitSignatureStorage storage = new BitSignatureInMemoryStorage();
		
		IndexPair[] indexPairs = new IndexPair[3];
		for (byte i = 0; i < indexPairs.length; i++) {
			long[] signature = new long[]{ i, i, i, i };
			IndexPair indexPair = new IndexPair(signature, i);
			indexPairs[i] = indexPair;
			storage.store(indexPair);
		}
		
		int i = 0;
		for (IndexPair pair : storage) {
			Assert.assertEquals(indexPairs[i], pair);
			i++;
		}
		
		Assert.assertEquals(3, i);
		
	}
}
