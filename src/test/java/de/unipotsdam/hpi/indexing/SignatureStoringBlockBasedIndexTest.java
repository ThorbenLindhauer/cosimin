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

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class SignatureStoringBlockBasedIndexTest extends AbstractBlockBasedIndexTest {

  @Override
  protected AbstractBlockBasedIndex<?> createIndex(Path basePath, int keySize,
      int blockSize) {
    return new SignatureStoringBlockBasedIndex(basePath, keySize, blockSize);
  }

  @Override
  protected IndexPair createIndexPair(long[] signature, int elementId) {
    return new IndexPair(signature, elementId);
  }
  
  @Test
  public void testGetNearestNeighbors() {
    int blockSize = 10;
    int keySize = 4;
    int numIndexPairs = 120;
    int searchIndex = 50;
    int beamSize = 20;
    AbstractBlockBasedIndex<?> index = createIndex(tempFolder, keySize, blockSize);

    IndexPair[] indexPairs = new IndexPair[numIndexPairs];
    for (int i = 0; i < numIndexPairs; i++) {
      long[] key = new long[] { 0, (byte) (0xFF & (i >> 8)),
          (byte) (0xFF & i), 0 };
      IndexPair pair = new IndexPair(key, i);
      indexPairs[i] = pair;
    }
    Arrays.sort(indexPairs, IndexPair.COMPARATOR);
    index.bulkLoad(indexPairs);

    IndexPair[] nearestNeighbours = index.getNearestNeighboursPairs(new long[] {
        0, (byte) (0xFF & (searchIndex >> 8)),
        (byte) (0xFF & searchIndex), 0 }, beamSize);
    Set<IndexPair> expectedNeighbours = new HashSet<IndexPair>();
    for (int i = 0; i < 2 * beamSize; i++) {
      expectedNeighbours.add(indexPairs[searchIndex - beamSize + i]);
    }
    for (IndexPair neighbour : nearestNeighbours) {
      String msg = String.format("Unexpected neighbour: %s", neighbour);
      Assert.assertTrue(msg, expectedNeighbours.remove(neighbour));
    }
    Assert.assertTrue("Elements not discovered: " + expectedNeighbours,
        expectedNeighbours.isEmpty());
  }

  @Test
  public void testGetNearestNeighborsFromTheFront() {
    int blockSize = 10;
    int keySize = 4;
    int numIndexPairs = 120;
    int beamSize = 20;
    AbstractBlockBasedIndex<?> index = createIndex(tempFolder, keySize, blockSize);

    IndexPair[] indexPairs = new IndexPair[numIndexPairs];
    for (int i = 0; i < numIndexPairs; i++) {
      long[] key = new long[] { 0, (byte) (0xFF & (i >> 8)),
          (byte) (0xFF & i), 1 };
      IndexPair pair = new IndexPair(key, i);
      indexPairs[i] = pair;
    }
    Arrays.sort(indexPairs, IndexPair.COMPARATOR);
    index.bulkLoad(indexPairs);

    IndexPair[] nearestNeighbours = index.getNearestNeighboursPairs(new long[] {
        0, 0, 0, 0 }, beamSize);
    Set<IndexPair> expectedNeighbours = new HashSet<IndexPair>();
    for (int i = 0; i < beamSize; i++) {
      expectedNeighbours.add(indexPairs[i]);
    }
    for (IndexPair neighbour : nearestNeighbours) {
      String msg = String.format("Unexpected neighbour: %s", neighbour);
      Assert.assertTrue(msg, expectedNeighbours.remove(neighbour));
    }
    Assert.assertTrue("Elements not discovered: " + expectedNeighbours,
        expectedNeighbours.isEmpty());
  }

  @Test
  public void testGetNearestNeighborsFromTheEnd() {
    int blockSize = 10;
    int keySize = 4;
    int numIndexPairs = 120;
    int beamSize = 20;
    AbstractBlockBasedIndex<?> index = createIndex(tempFolder, keySize, blockSize);

    IndexPair[] indexPairs = new IndexPair[numIndexPairs];
    for (byte i = 0; i < numIndexPairs; i++) {
      long[] key = new long[] { i, i, i, i };
      IndexPair pair = new IndexPair(key, i);
      indexPairs[i] = pair;
    }
    Arrays.sort(indexPairs, IndexPair.COMPARATOR);
    index.bulkLoad(indexPairs);

    IndexPair[] nearestNeighbours = index.getNearestNeighboursPairs(new long[] {
        -128, -128, -128, -128 }, beamSize);
    Set<IndexPair> expectedNeighbours = new HashSet<IndexPair>();
    for (int i = indexPairs.length - beamSize; i < indexPairs.length; i++) {
      expectedNeighbours.add(indexPairs[i]);
    }
    for (IndexPair neighbour : nearestNeighbours) {
      String msg = String.format("Unexpected neighbour: %s", neighbour);
      Assert.assertTrue(msg, expectedNeighbours.remove(neighbour));
    }
    Assert.assertTrue("Elements not discovered: " + expectedNeighbours,
        expectedNeighbours.isEmpty());  
  }
}
