package de.unipotsdam.hpi.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Profiler for measuring execution times. Offers static methods to ease access
 * when inserting profiling code.
 * 
 * @author Sebastian
 * 
 */
public class Profiler {

	private static Map<String, Record> records = new HashMap<String, Record>();

	/**
	 * Start or resume the timer for the given key.
	 */
	public static void start(String key) {
		Record record = records.get(key);
		if (record == null) {
			record = new Record();
			records.put(key, record);
		}
		record.startTime = System.currentTimeMillis();
	}

	/**
	 * Stop the timer with the given key.
	 */
	public static void stop(String key) {
		long stopTime = System.currentTimeMillis();
		Record record = records.get(key);
		if (key == null) {
			System.err.println("No profiling item for " + key);
		} else if (record.startTime == -1) {
			System.err.println("Timer was not started for " + key);
		} else {
			record.accumulatedTime += stopTime - record.startTime;
			record.startTime = -1;
		}
	}

	/**
	 * Returns the time for the recorded time associated with the given key or
	 * -1 if no such item exists.
	 */
	public static long getTime(String key) {
		Record record = records.get(key);
		if (record == null)
			return -1;
		return record.accumulatedTime;
	}

	/**
	 * Prints the measurements to the standard output.
	 */
	public static void printMeasurements() {
		System.out.println("Profiler Results:");
		for (Map.Entry<String, Record> entry : records.entrySet()) {
			System.out.format("%40s %s\n", entry.getKey(),
					formatTime(entry.getValue().accumulatedTime));
		}
	}

	public static String formatTime(long time) {
		long completeMs = time;
		long ms = time % 1000;
		time /= 1000;
		long s = time % 60;
		time /= 60;
		long mins = time % 60;
		time /= 60;
		long hours = time;
		return String.format("%dh - %2dmins - %2ds - %03dms (%d)", hours, mins,
				s, ms, completeMs);
	}

	private static class Record {
		private long startTime = -1;
		private long accumulatedTime;
	}

	public static void clear() {
		records.clear();
	}

}
