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
