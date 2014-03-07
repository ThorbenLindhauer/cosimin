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
