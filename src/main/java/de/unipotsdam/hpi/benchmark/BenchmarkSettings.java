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

public class BenchmarkSettings extends AbstractSettings {
  
  @Property("benchmark.query.count")
  public int numQueries;
  
  @Property("benchmark.query.beam")
  public int beamSize;
  
  @Property("benchmark.query.minsimilarity")
  public double minSimilarity;
  
  @Property("benchmark.mutate.query.vectors")
  public boolean mutateQueryVectors;
  
  @Property("benchmark.sync.queries")
  public boolean performSyncQueries;
  
  @Property("benchmark.async.queries")
  public boolean performAsyncQueries;
  
  @Property("benchmark.query.threads")
  public int numQueryThreads;

  @Property("input.vectors.range")
  public int vectorComponentRange;
  
  @Property("input.vectors.file")
  public String inputVectorFilePath;
  
  @Property("input.vectors.count")
  public int numInputVectors;
  
  @Property("benchmark.streaming.chunksize")
  public int streamingChunkSize;
  
  public int getVectorComponentRange() {
    return vectorComponentRange;
  }

  public void setVectorComponentRange(int vectorComponentRange) {
    this.vectorComponentRange = vectorComponentRange;
  }

  public int getNumQueries() {
    return numQueries;
  }

  public void setNumQueries(int numQueries) {
    this.numQueries = numQueries;
  }

  public int getBeamSize() {
    return beamSize;
  }

  public void setBeamSize(int beamSize) {
    this.beamSize = beamSize;
  }

  public double getMinSimilarity() {
    return minSimilarity;
  }

  public void setMinSimilarity(double minSimilarity) {
    this.minSimilarity = minSimilarity;
  }

  public boolean isMutateQueryVectors() {
    return mutateQueryVectors;
  }

  public void setMutateQueryVectors(boolean mutateQueryVectors) {
    this.mutateQueryVectors = mutateQueryVectors;
  }

  public boolean isPerformSyncQueries() {
    return performSyncQueries;
  }

  public void setPerformSyncQueries(boolean performSyncQueries) {
    this.performSyncQueries = performSyncQueries;
  }

  public boolean isPerformAsyncQueries() {
    return performAsyncQueries;
  }

  public void setPerformAsyncQueries(boolean performAsyncQueries) {
    this.performAsyncQueries = performAsyncQueries;
  }

  public String getInputVectorFilePath() {
    return inputVectorFilePath;
  }

  public void setInputVectorFilePath(String inputVectorFilePath) {
    this.inputVectorFilePath = inputVectorFilePath;
  }

  public int getNumInputVectors() {
    return numInputVectors;
  }

  public void setNumInputVectors(int numInputVectors) {
    this.numInputVectors = numInputVectors;
  }

  public void setStreamingChunkSize(int streamingChunkSize) {
    this.streamingChunkSize = streamingChunkSize;
  }

  public int getStreamingChunkSize() {
    return streamingChunkSize;
  }

  public int getNumQueryThreads() {
    return numQueryThreads;
  }

  public void setNumQueryThreads(int numQueryThreads) {
    this.numQueryThreads = numQueryThreads;
  }
  
}
