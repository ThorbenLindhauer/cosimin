package de.unipotsdam.hpi.indexing;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;

import de.unipotsdam.hpi.permutation.PermutationFunction;
import de.unipotsdam.hpi.storage.AggregatedReferenceBlockStorage;
import de.unipotsdam.hpi.storage.BitSignatureIndex;
import de.unipotsdam.hpi.util.BitSignatureUtil;

public class ReferenceBlockBasedIndex extends AbstractBlockBasedIndex<ReferenceBlock> {

  private static final long serialVersionUID = 1L;
  
  transient protected BitSignatureIndex bitSignatureIndex;
  transient protected PermutationFunction permutationFunction;
  
  private int storageCounter = 0;
  private int blocksPerFile = 100;
  
  transient AggregatedReferenceBlockStorage currentStorage;
  
  public ReferenceBlockBasedIndex(Path basePath, int keySize, int blockSize, BitSignatureIndex bitSignatureIndex, 
      PermutationFunction permutationFunction) {
    super(basePath, keySize, blockSize);
    this.bitSignatureIndex = bitSignatureIndex;
    this.permutationFunction = permutationFunction;
  }

  protected ReferenceBlock createNewBlock() throws IOException {
    AggregatedReferenceBlockStorage storage = resolveBlockStorage();
    ReferenceBlock block = new ReferenceBlock(blockSize, keySize, storage, blockIdCounter++);
    return block;
  }
  
  protected AggregatedReferenceBlockStorage resolveBlockStorage() {
    if (currentStorage == null || storageCounter % blocksPerFile == 0) {
      int storageId = storageCounter / blocksPerFile;
      Path storagePath = basePath.resolve("blockIndex" + storageId);
      currentStorage = new AggregatedReferenceBlockStorage(storagePath);
    }
    storageCounter++;
    
    return currentStorage;
  }
  
  /**
   * This is anyway a very expensive operation, since it requires to apply the permutation function.
   */
  public IndexPair[] getNearestNeighboursPairs(long[] key, int beamRadius) {
    throw new UnsupportedOperationException("not yet implemented");
  }
  
  /**
   * Will return a super set of the elements within the beamRadius 
   * 
   * TODO: Returns non-permuted signature which breaks the API
   */
  public int[] getNearestNeighboursElementIds(long[] key, int beamRadius) {
    ReferenceBlock containingBlock = getBlockFor(key);
    ReferenceBlock lowerBlock = null;
    ReferenceBlock higherBlock = null;
    
    IntList neighbours = new IntArrayList(3 * blockSize);
    
    if (containingBlock == null) {
      higherBlock = firstBlock;
    } else {
      neighbours.addElements(neighbours.size(), containingBlock.getElements());
      
      lowerBlock = containingBlock.getPreviousBlock();
      higherBlock = containingBlock.getNextBlock();
    }
    
    int fetchSmallerElements = beamRadius, fetchGreaterElements = beamRadius;
    
    while (lowerBlock != null && fetchSmallerElements > 0) {
      neighbours.addElements(neighbours.size(), lowerBlock.getElements());

      fetchSmallerElements -= lowerBlock.getSize();
      lowerBlock = lowerBlock.getPreviousBlock();
    }
    
    while (higherBlock != null && fetchGreaterElements > 0) {
      neighbours.addElements(neighbours.size(), higherBlock.getElements());
      
      fetchGreaterElements -= higherBlock.getSize();
      higherBlock = higherBlock.getNextBlock();
    }
    
    return neighbours.toArray(new int[neighbours.size()]);
  }
  
  @Override
  public void insertElement(IndexPair pair) throws IOException {
    if (!bitSignatureIndex.contains(pair.getElementId())) {
      throw new RuntimeException("Element not in bit signature index: "+ pair);
    }
    
    // If there is no block yet, allocate one and insert the element.
    if (firstBlock == null) {
      firstBlock = createNewBlock();
      firstBlock.insertElement(pair.getElementId(), pair.getBitSignature(), 0);
    } else {
      ReferenceBlock block = getBlockFor(pair.getBitSignature());
      if (block == null) {
        // If there is no block that might contain the pair, all blocks
        // have a greater start key.
        // So add to the first block.
        block = firstBlock;
      }
      if (block.getSize() < block.getCapacity()) {
        int position = determineBlockPositionForElement(block, pair);
        block.insertElement(pair.getElementId(), pair.getBitSignature(), position);
      } else {
        // Split the block and add the elements:
        // 1. Create a new block and link it right after the current
        // block.
        ReferenceBlock newBlock = createNewBlock();
        newBlock.setPreviousBlock(block);
        newBlock.setNextBlock(block.getNextBlock());
        block.setNextBlock(newBlock);
        if (newBlock.getNextBlock() != null) {
          newBlock.getNextBlock().setPreviousBlock(newBlock);
        }

        // 2. Retrieve all elements and spread them over the new blocks.
        IndexPair[] pairs = resolveBlock(block);
        int splitIndex = pairs.length / 2 + 1;
        bulkLoadBlock(block, pairs, 0, splitIndex);
        bulkLoadBlock(newBlock, pairs, splitIndex, pairs.length - splitIndex);

        // 3. Find the target block for the new element and add it.
        if (BitSignatureUtil.COMPARATOR.compare(pair.getBitSignature(),
            newBlock.getStartKey()) < 0) {
          int insertIndex = determineBlockPositionForElement(block, pair);
          block.insertElement(pair.getElementId(), pair.getBitSignature(), insertIndex);
        } else {
          int insertIndex = determineBlockPositionForElement(newBlock, pair);
          newBlock.insertElement(pair.getElementId(), pair.getBitSignature(), insertIndex);
        }
      }
    }
  }
  
  protected int determineBlockPositionForElement(ReferenceBlock block, IndexPair pair) {
    IndexPair[] resolvedBlock = resolveBlock(block);
    
    for (int i = 0; i < resolvedBlock.length; i++) {
      IndexPair resolvedPair = resolvedBlock[i];
      
      if (BitSignatureUtil.COMPARATOR.compare(resolvedPair.getBitSignature(), pair.getBitSignature()) > 0) {
        return i;
      }
    }
    
    return resolvedBlock.length;
  }
  
  /**
   * Gets elements with permuted signatures for the supplied block.
   * 
   * @param block
   * @return
   */
  protected IndexPair[] resolveBlock(ReferenceBlock block) {
    int[] elementIds = block.getElements();
    IndexPair[] resolvedElements = new IndexPair[elementIds.length];
    
    for (int i = 0; i < elementIds.length; i++) {
      int elementId = elementIds[i];
      IndexPair pair = bitSignatureIndex.getIndexPair(elementId);
      long[] permutedSignature = permutationFunction.permute(pair.getBitSignature());
      resolvedElements[i] = new IndexPair(permutedSignature, elementId);
    }
    
    return resolvedElements;
  }
  
  protected void bulkLoadBlock(ReferenceBlock block, IndexPair[] pairs, int offset, int length) {
    if (length == 0) {
      return;
    }
    
    int[] elementIds = new int[pairs.length];
    
    for (int i = 0; i < pairs.length; i++) {
      IndexPair pair = pairs[i];
      
      if (!bitSignatureIndex.contains(pair.getElementId())) {
        throw new RuntimeException("Element not in bit signature index: "+ pair);
      }
      
      elementIds[i] = pair.getElementId();
    }
    
    block.bulkLoad(elementIds, offset, length, pairs[offset].getBitSignature());
  }
  
  @Override
  public void deleteElement(long[] key) {
    throw new UnsupportedOperationException("not implemented");
    
  }

  @Override
  public int getElement(long[] key) {
    ReferenceBlock block = getBlockFor(key);
    if (block == null)
      throw new IllegalArgumentException("Key not present in index.");
    
    int[] elementIds = block.getElements();
    
    for (int elementId : elementIds) {
      long[] associatedKey = bitSignatureIndex.getIndexPair(elementId).getBitSignature();
      long[] permutedKey = permutationFunction.permute(associatedKey);
      if (BitSignatureUtil.COMPARATOR.compare(key, permutedKey) == 0) {
        return elementId;
      }
    }
    
    throw new IllegalArgumentException("No such entry found in the block: "
        + Arrays.toString(key));
  }

  
  /**
   * Currently not implemented. An implementation would have to resolve the permuted signatures of all contained blocks
   * which is anyway a rather intensive task, such that iterating over all IndexPairs is not recommended for instances of this index.
   */
  @Override
  public Iterator<IndexPair> iterator() {
    throw new UnsupportedOperationException("not implemented");
  }

  public void setBitSignatureIndex(BitSignatureIndex bitSignatureIndex) {
    this.bitSignatureIndex = bitSignatureIndex;
  }

  public void setPermutationFunction(PermutationFunction permutationFunction) {
    this.permutationFunction = permutationFunction;
  }
}
