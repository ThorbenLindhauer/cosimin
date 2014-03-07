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

import java.util.concurrent.Future;

public class BenchmarkQueryResult {

	private int itemId;
	private Future<Int2DoubleMap> futureResult;
	
	public BenchmarkQueryResult(int itemId, Future<Int2DoubleMap> futureResult) {
		this.itemId = itemId;
		this.futureResult = futureResult;
	}
	public int getItemId() {
		return itemId;
	}
	public Future<Int2DoubleMap> getFutureResult() {
		return futureResult;
	}
}
