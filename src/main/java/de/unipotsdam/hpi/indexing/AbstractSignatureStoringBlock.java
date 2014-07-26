package de.unipotsdam.hpi.indexing;

public abstract class AbstractSignatureStoringBlock 
  extends AbstractLinkedBlock<SignatureStoringBlock> implements SignatureStoringBlock {

  private static final long serialVersionUID = 1L;

  public void bulkLoad(IndexPair[] pairs) {
    bulkLoad(pairs, 0, pairs.length);
    
  }
}
