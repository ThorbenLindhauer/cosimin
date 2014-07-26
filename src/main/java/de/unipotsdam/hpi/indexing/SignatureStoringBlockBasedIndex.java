package de.unipotsdam.hpi.indexing;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.unipotsdam.hpi.util.BitSignatureUtil;

public class SignatureStoringBlockBasedIndex extends AbstractBlockBasedIndex<SignatureStoringBlock> {

  public SignatureStoringBlockBasedIndex(Path basePath, int keySize,
      int blockSize) {
    super(basePath, keySize, blockSize);
  }

  private static final long serialVersionUID = 1L;

  protected SignatureStoringBlock createNewBlock() throws IOException {
    Path blockPath = basePath.resolve("blockIndex" + blockIdCounter++);
    CachingBlock block = new CachingBlock(blockSize, keySize, blockPath);
    return block;
  }
  
  public void insertElement(IndexPair pair) throws IOException {
    // If there is no block yet, allocate one and insert the element.
    if (firstBlock == null) {
      firstBlock = createNewBlock();
      firstBlock.insertElement(pair);
    } else {
      SignatureStoringBlock block = getBlockFor(pair.getBitSignature());
      if (block == null) {
        // If there is no block that might contain the pair, all blocks
        // have a greater start key.
        // So add to the first block.
        block = firstBlock;
      }
      if (block.getSize() < block.getCapacity()) {
        block.insertElement(pair);
      } else {
        // Split the block and add the elements:
        // 1. Create a new block and link it right after the current
        // block.
        SignatureStoringBlock newBlock = createNewBlock();
        newBlock.setPreviousBlock(block);
        newBlock.setNextBlock(block.getNextBlock());
        block.setNextBlock(newBlock);
        if (newBlock.getNextBlock() != null) {
          newBlock.getNextBlock().setPreviousBlock(newBlock);
        }

        // 2. Retrieve all elements and spread them over the new blocks.
        IndexPair[] pairs = block.getElements();
        int splitIndex = pairs.length / 2 + 1;
        block.bulkLoad(pairs, 0, splitIndex);
        newBlock.bulkLoad(pairs, splitIndex, pairs.length - splitIndex);

        // 3. Find the target block for the new element and add it.
        if (BitSignatureUtil.COMPARATOR.compare(pair.getBitSignature(),
            newBlock.getStartKey()) < 0) {
          block.insertElement(pair);
        } else {
          newBlock.insertElement(pair);
        }
      }
    }
  }

  public void deleteElement(long[] key) {
    SignatureStoringBlock block = getBlockFor(key);
    if (block != null) {
      block.deleteElement(key);
    }
  }
  
  public int[] getNearestNeighboursElementIds(long[] key, int beamRadius) {
    throw new UnsupportedOperationException("not implemented");
  }
  
  public IndexPair[] getNearestNeighboursPairs(long[] key, int beamRadius) {
    SignatureStoringBlock block = getBlockFor(key);
    int fetchSmallerElements = beamRadius, fetchGreaterElements = beamRadius;
    SignatureStoringBlock smallerBlock = null, greaterBlock = null;
    List<IndexPair> neighbours = new ArrayList<IndexPair>(2 * beamRadius);
    if (block == null) {
      // All blocks' start keys are greater than the given key. So, the
      // best we can do, is to fetch all small elements.
      greaterBlock = firstBlock;
    } else {
      // Fetch the elements, find the middle of the beam, add the elements
      // from this block, and tell how many elements to fetch from other
      // blocks.
      IndexPair[] blockElements = block.getElements();
      int beamCenter = 0;
      for (IndexPair pair : blockElements) {
        if (BitSignatureUtil.COMPARATOR.compare(key, pair.getBitSignature()) <= 0) {
          break;
        }
        beamCenter++;
      }
      int startIndex = Math.max(0, beamCenter - beamRadius);
      int endIndex = Math.min(blockElements.length, beamCenter + beamRadius);
      for (int i = startIndex; i < endIndex; i++) {
        neighbours.add(blockElements[i]);
      }
      fetchSmallerElements -= (beamCenter - startIndex);
      smallerBlock = block.getPreviousBlock();
      
      fetchGreaterElements -= (endIndex - beamCenter);
      greaterBlock = block.getNextBlock();
    }
    
    // Fetch necessary elements from surrounding blocks.
    while (smallerBlock != null && fetchSmallerElements > 0) {
      int startIndex = Math.max(smallerBlock.getSize() - fetchSmallerElements, 0);
      int numElements = smallerBlock.getSize() - startIndex;
      IndexPair[] blockElements = smallerBlock.getElements(startIndex, numElements);
      for (IndexPair pair : blockElements) {
        neighbours.add(pair);
      }
      fetchSmallerElements -= numElements;
      smallerBlock = smallerBlock.getPreviousBlock();
    }
    while (greaterBlock != null && fetchGreaterElements > 0) {
      int numElements = Math.min(greaterBlock.getSize(), fetchGreaterElements);
      IndexPair[] blockElements = greaterBlock.getElements(0, numElements);
      for (IndexPair pair : blockElements) {
        neighbours.add(pair);
      }
      fetchGreaterElements -= numElements;
      greaterBlock = greaterBlock.getNextBlock();
    }
    
    return neighbours.toArray(new IndexPair[neighbours.size()]);
  }
  
  public void bulkLoad(IndexPair[] keyValuePairs) {
    try {
      int pairsPerBlock = (int) Math
          .ceil(INITIAL_LOAD_FACTOR * blockSize);
      // ceil keyValuePairs.length / pairsPerBlock
      int numBlocks = (keyValuePairs.length + pairsPerBlock - 1)
          / pairsPerBlock;
      SignatureStoringBlock currentBlock = null;
      SignatureStoringBlock lastBlock = null;
      int offset = 0;
      int remainingPairs = keyValuePairs.length;
      for (int i = 0; i < numBlocks; i++) {
        currentBlock = createNewBlock();

        if (firstBlock == null) {
          firstBlock = currentBlock;
        }

        if (lastBlock != null) {
          lastBlock.setNextBlock(currentBlock);
          currentBlock.setPreviousBlock(lastBlock);
        }
        lastBlock = currentBlock;

        int pairsToWrite = Math.min(remainingPairs, pairsPerBlock);
        currentBlock.bulkLoad(keyValuePairs, offset, pairsToWrite);
        remainingPairs -= pairsToWrite;
        offset += pairsToWrite;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public int getElement(long[] key) {
    SignatureStoringBlock block = getBlockFor(key);
    if (block == null)
      throw new IllegalArgumentException("Key not present in index.");
    return block.get(key);
  }
  
  public Iterator<IndexPair> iterator() {
    return new IndexIterator(firstBlock);
  }
  
  @Override
  protected void bulkLoadBlock(SignatureStoringBlock block, IndexPair[] pairs,
      int offset, int pairsToWrite) throws IOException {
    block.bulkLoad(pairs, offset, pairsToWrite);
  }
  
  private class IndexIterator implements Iterator<IndexPair> {

    private SignatureStoringBlock curBlock;
    private int curIndex;
    private IndexPair[] curIndexPairs;
    private IndexPair next;

    public IndexIterator(SignatureStoringBlock startBlock) {
      if (startBlock == null)
        return;
      this.curBlock = startBlock;
      this.curIndexPairs = startBlock.getElements();
      if (this.curIndexPairs.length == 0)
        move();
      else {
        this.curIndex = 0;
        this.next = this.curIndexPairs[0];
      }
    }

    private void move() {
      while (true) {
        if (++curIndex < curIndexPairs.length) {
          next = curIndexPairs[curIndex];
          return;
        } else {
          curBlock = curBlock.getNextBlock();
          if (curBlock == null) {
            next = null;
            return;
          }
          curIndexPairs = curBlock.getElements();
          curIndex = -1;
        }
      }
    }

    public boolean hasNext() {
      return next != null;
    }

    public IndexPair next() {
      IndexPair curNext = next;
      move();
      return curNext;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

  }
}
