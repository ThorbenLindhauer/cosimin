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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

import de.unipotsdam.hpi.indexing.BlockBasedIndex;
import de.unipotsdam.hpi.indexing.Index;
import de.unipotsdam.hpi.indexing.IndexPair;
import de.unipotsdam.hpi.util.BitSignatureUtil;
import de.unipotsdam.hpi.util.FileUtils;
import de.unipotsdam.hpi.util.Profiler;

public class IndexCreationBenchmark {

  private static final Logger logger = Logger.getLogger(IndexCreationBenchmark.class.getName());
  
	private IndexBenchmarkSettings settings;

	public IndexCreationBenchmark(IndexBenchmarkSettings settings) {
		this.settings = settings;
	}
	
	public void run() throws IOException {
		IndexPair[] indexPairs = generateBitSignatures();
		sortIndexPairs(indexPairs);
		createIndex(indexPairs);
		
		Profiler.printMeasurements();
	}

	private void createIndex(IndexPair[] indexPairs) throws IOException {
		logger.info("Creating index");
		
		Path basePath = FileSystems.getDefault().getPath(settings.getIndexPath());
		int keySize = settings.getBitSignatureLength() >> BitSignatureUtil.LOG_BASE_TYPE_SIZE;;
		int blockSize = settings.getBlockSize();
		FileUtils.createDirectoryIfNotExists(basePath);
		FileUtils.clearDirectory(basePath);
		
		Profiler.start("Build index");
		Index index = new BlockBasedIndex(basePath, keySize, blockSize);
		index.bulkLoad(indexPairs);
		index.close();
		Profiler.stop("Build index");
	}

	private void sortIndexPairs(IndexPair[] indexPairs) {
	  logger.info("Sorting index pairs");
		
		Profiler.start("Sort signatures");
		Arrays.sort(indexPairs, IndexPair.COMPARATOR);
		Profiler.stop("Sort signatures");
	}
	
	private IndexPair[] generateBitSignatures() {
	  logger.info("Generating bit signatures");
		
		int numBitSignatures = settings.getNumBitSignatures();
		int numSignatureParts = settings.getBitSignatureLength() >> BitSignatureUtil.LOG_BASE_TYPE_SIZE;
		
		Profiler.start("Create signatures");
		Random random = new Random();
		IndexPair[] indexPairs = new IndexPair[numBitSignatures];
		for (int i = 0; i < numBitSignatures; i++) {
			long[] bitSignature = new long[numSignatureParts];
			for (int j = 0; j < numSignatureParts; j++) {
				bitSignature[j] = random.nextLong();
			}
			indexPairs[i] = new IndexPair(bitSignature, i);
		}
		Profiler.stop("Create signatures");
		
		return indexPairs;
	}

	/**
	 * @param args path to a properties file containing the settings
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		IndexBenchmarkSettings settings = new IndexBenchmarkSettings();
		settings.load(args[0]);
		new IndexCreationBenchmark(settings).run();
	}

}
