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
