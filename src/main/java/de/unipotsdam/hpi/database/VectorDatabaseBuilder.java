package de.unipotsdam.hpi.database;

import java.io.IOException;
import java.util.Iterator;

import de.unipotsdam.hpi.input.InputVector;

public class VectorDatabaseBuilder {

  private Settings vdbSettings = new Settings();
  private Iterator<InputVector> inputVectors;
  
  public VectorDatabaseBuilder alphabetSize(int size) {
    vdbSettings.setInputVectorSize(size);
    return this;
  }

  public VectorDatabaseBuilder numHyperplanes(int numHyperplanes) {
    vdbSettings.setLshSize(numHyperplanes);
    return this;
  }

  public VectorDatabaseBuilder numPermutations(int numPermutations) {
    vdbSettings.setNumPermutations(numPermutations);
    return this;
  }

  public VectorDatabaseBuilder basePath(String basePath) {
    vdbSettings.setBasePath(basePath);
    return this;
  }
  
  public VectorDatabaseBuilder data(Iterator<InputVector> inputVectors) {
    this.inputVectors = inputVectors;
    return this;
  }
  
  public VectorDatabaseBuilder indexBlockSize(int indexBlockSize) {
    vdbSettings.setBlockSize(indexBlockSize);
    return this;
  }
  
  public VectorDatabaseBuilder parallelSorting(boolean parallelSorting) {
    vdbSettings.setPerformParallelSorting(parallelSorting);
    return this;
  }
  
  public VectorDatabaseBuilder parallelLSH(boolean parallelLsh) {
    vdbSettings.setPerformParallelLsh(parallelLsh);
    return this;
  }
  
  public VectorDatabase buildVectorDatabase() {
    try {
      VectorDatabase vdb = new VectorDatabase(vdbSettings);
      vdb.bulkLoad(inputVectors);
      return vdb;
    } catch (IOException e) {
      throw new RuntimeException("Cannot build vector database", e);
    }
  }

  public VectorDatabase recoverVectorDatabase() {
    try {
      VectorDatabase vdb = new VectorDatabase(vdbSettings);
      vdb.recover();
      return vdb;
    } catch (IOException e) {
      throw new RuntimeException("Cannot recover vector database", e);
    }
  }

}
