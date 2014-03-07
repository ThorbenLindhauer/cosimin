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
