package de.unipotsdam.hpi.indexing;


public abstract class AbstractSignatureStoringBlockTest extends AbstractIndexBlockTest<SignatureStoringBlock> {


  @Override
  protected void bulkLoad(SignatureStoringBlock block, IndexPair[] pairs) {
    block.bulkLoad(pairs);
    
  }

  @Override
  protected void insertElement(SignatureStoringBlock block, IndexPair pair) {
    block.insertElement(pair);
    
  }

  @Override
  protected IndexPair[] getBlockElements(SignatureStoringBlock block, int offset,
      int length) {
    return block.getElements(offset, length);
  }
}
