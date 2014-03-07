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
package de.unipotsdam.hpi.benchmark;

import de.unipotsdam.hpi.util.AbstractSettings;
import de.unipotsdam.hpi.util.Property;

public class IndexBenchmarkSettings extends AbstractSettings {

  @Property("input.bitsignatures.count")
  public int numBitSignatures;
  
  @Property("input.bitsignatures.length")
  public int bitSignatureLength;
  
  @Property("index.path")
  public String indexPath;
  
  @Property("index.block.size")
  public int blockSize;
  
  @Property("sorting.parallel")
  public boolean performParallelSorting;

  public int getNumBitSignatures() {
    return numBitSignatures;
  }

  public void setNumBitSignatures(int numBitSignatures) {
    this.numBitSignatures = numBitSignatures;
  }

  public int getBitSignatureLength() {
    return bitSignatureLength;
  }

  public void setBitSignatureLength(int bitSignatureLength) {
    this.bitSignatureLength = bitSignatureLength;
  }

  public String getIndexPath() {
    return indexPath;
  }

  public void setIndexPath(String indexPath) {
    this.indexPath = indexPath;
  }

  public int getBlockSize() {
    return blockSize;
  }

  public void setBlockSize(int blockSize) {
    this.blockSize = blockSize;
  }

  public boolean isPerformParallelSorting() {
    return performParallelSorting;
  }

  public void setPerformParallelSorting(boolean performParallelSorting) {
    this.performParallelSorting = performParallelSorting;
  }
}
