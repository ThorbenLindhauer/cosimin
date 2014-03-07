package de.unipotsdam.hpi.database;

import de.unipotsdam.hpi.util.AbstractSettings;
import de.unipotsdam.hpi.util.Property;



/**
 * Contains the parameters used in this application.
 * @author Sebastian
 *
 */
public class Settings extends AbstractSettings {

  @Property("lsh.size")
  public int lshSize = 1;

  @Property("index.block.size")
  public int blockSize = 1;

  @Property("index.permutations")
  public int numPermutations = 1;

  @Property("path")
  public String basePath = ".vdb";

  @Property("store_signatures")
  public boolean saveBitSignatures;

  @Property("lsh.parallel")
  public boolean performParallelLsh = true;

  @Property("sorting.parallel")
  public boolean performParallelSorting = true;
	
  @Property("input.size")
  public int inputVectorSize = 1;
	
	public int getLshSize() {
		return lshSize;
	}

	public void setLshSize(int lshSize) {
		this.lshSize = lshSize;
	}

	public int getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	public int getNumPermutations() {
		return numPermutations;
	}

	public void setNumPermutations(int numPermutations) {
		this.numPermutations = numPermutations;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public boolean isSaveBitSignatures() {
		return this.saveBitSignatures;
	}

	public void setSaveBitSignatures(boolean saveBitSignatures) {
		this.saveBitSignatures = saveBitSignatures;
	}

	public boolean isPerformParallelLsh() {
		return performParallelLsh;
	}

	public void setPerformParallelLsh(boolean performParallelLsh) {
		this.performParallelLsh = performParallelLsh;
	}

	public boolean isPerformParallelSorting() {
		return performParallelSorting;
	}

	public void setPerformParallelSorting(boolean performParallelSorting) {
		this.performParallelSorting = performParallelSorting;
	}

  public int getInputVectorSize() {
    return inputVectorSize;
  }

  public void setInputVectorSize(int inputVectorSize) {
    this.inputVectorSize = inputVectorSize;
  }

}
