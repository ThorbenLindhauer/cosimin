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
package de.unipotsdam.hpi.util;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Profiler for measuring execution times. Offers static methods to ease access
 * when inserting profiling code.
 * 
 * @author Sebastian
 * 
 */
public class Profiler {

  private static final Logger logger = Logger.getLogger(Profiler.class.getName());

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
	  StringBuilder profilerResults = new StringBuilder();
	  profilerResults.append("Profiler Results:\n");
		for (Map.Entry<String, Record> entry : records.entrySet()) {
		  profilerResults.append(String.format("%40s %s\n", entry.getKey(),
					formatTime(entry.getValue().accumulatedTime)));
		}
		logger.info(profilerResults.toString());
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
