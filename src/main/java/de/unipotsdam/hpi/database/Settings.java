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
