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

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.nio.file.Path;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.unipotsdam.hpi.permutation.NullPermutationFunction;
import de.unipotsdam.hpi.storage.BitSignatureIndex;

public class ReferenceBlockBasedIndexTest extends AbstractBlockBasedIndexTest {

  protected BitSignatureIndex bitSignatureIndex;
  
  @Before
  public void setUpBitSignatureIndex() {
    bitSignatureIndex = new BitSignatureIndex();
  }
  
  @Override
  protected AbstractBlockBasedIndex<?> createIndex(Path basePath, int keySize,
      int blockSize) {
    return new ReferenceBlockBasedIndex(basePath, keySize, blockSize, bitSignatureIndex, new NullPermutationFunction());
  }
  

  @Override
  protected IndexPair createIndexPair(long[] signature, int elementId) {
    IndexPair pair = new IndexPair(signature, elementId);
    bitSignatureIndex.add(pair);
    return pair;
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
      IndexPair pair = createIndexPair(key, i);
      indexPairs[i] = pair;
    }
    Arrays.sort(indexPairs, IndexPair.COMPARATOR);
    index.bulkLoad(indexPairs);

    int[] nearestNeighbours = index.getNearestNeighboursElementIds(new long[] {
        0, (byte) (0xFF & (searchIndex >> 8)),
        (byte) (0xFF & searchIndex), 0 }, beamSize);
    IntSet expectedNeighbours = new IntOpenHashSet();
    for (int i = 0; i < 2 * beamSize; i++) {
      expectedNeighbours.add(indexPairs[searchIndex - beamSize + i].getElementId());
    }
    for (int neighbour : nearestNeighbours) {
      expectedNeighbours.remove(neighbour);
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
      IndexPair pair = createIndexPair(key, i);
      indexPairs[i] = pair;
    }
    Arrays.sort(indexPairs, IndexPair.COMPARATOR);
    index.bulkLoad(indexPairs);

    int[] nearestNeighbours = index.getNearestNeighboursElementIds(new long[] {
        0, 0, 0, 0 }, beamSize);
    IntSet expectedNeighbours = new IntOpenHashSet();
    for (int i = 0; i < beamSize; i++) {
      expectedNeighbours.add(indexPairs[i].getElementId());
    }
    for (int neighbour : nearestNeighbours) {
      expectedNeighbours.remove(neighbour);
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
      IndexPair pair = createIndexPair(key, i);
      indexPairs[i] = pair;
    }
    Arrays.sort(indexPairs, IndexPair.COMPARATOR);
    index.bulkLoad(indexPairs);

    int[] nearestNeighbours = index.getNearestNeighboursElementIds(new long[] {
        -128, -128, -128, -128 }, beamSize);
    IntSet expectedNeighbours = new IntOpenHashSet();
    for (int i = indexPairs.length - beamSize; i < indexPairs.length; i++) {
      expectedNeighbours.add(indexPairs[i].getElementId());
    }
    for (int neighbour : nearestNeighbours) {
      expectedNeighbours.remove(neighbour);
    }
    Assert.assertTrue("Elements not discovered: " + expectedNeighbours,
        expectedNeighbours.isEmpty());  
  }
  
  
  // both test cases are still pending as they are not implemented for this index yet
  @Ignore
  public void testDeleteElement() {
  }
  
  @Ignore
  public void testIndexBulkLoadingInsertsElements() {
  }
}
