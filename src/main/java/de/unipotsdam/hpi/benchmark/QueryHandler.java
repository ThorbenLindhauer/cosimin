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

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

import java.util.concurrent.Callable;

import de.unipotsdam.hpi.database.VectorDatabase;
import de.unipotsdam.hpi.input.InputVector;

public class QueryHandler implements Callable<Int2DoubleMap> {

	private VectorDatabase database;
	private InputVector queryVector;
	private	int beamRadius;
	private double minSimilarity;
	
	public QueryHandler(VectorDatabase database, InputVector queryVector,
			int beamRadius, double minSimilarity) {
		this.database = database;
		this.queryVector = queryVector;
		this.beamRadius = beamRadius;
		this.minSimilarity = minSimilarity;
	}
	
	public Int2DoubleMap call() throws Exception {
		return database.getNearNeighborsWithDistance(queryVector, beamRadius, minSimilarity);
	}

}
