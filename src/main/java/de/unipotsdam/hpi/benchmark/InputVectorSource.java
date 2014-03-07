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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.util.Iterator;

import de.unipotsdam.hpi.database.Settings;
import de.unipotsdam.hpi.input.InputVector;
import de.unipotsdam.hpi.input.IntArrayInputVector;
import de.unipotsdam.hpi.util.EncodingUtils;
import de.unipotsdam.hpi.util.FileUtils;
import de.unipotsdam.hpi.util.Profiler;

public class InputVectorSource {

	private Settings settings;
	private BenchmarkSettings benchmarkSettings;

	public InputVectorSource(Settings settings, BenchmarkSettings benchmarkSettings) {
		this.settings = settings;
		this.benchmarkSettings = benchmarkSettings;
	}

	public void generateVectors() throws IOException {
		File file = new File(benchmarkSettings.getInputVectorFilePath());
		FileUtils.createDirectoryIfNotExists(FileSystems.getDefault().getPath(
				file.getParent().toString()));
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		Iterator<InputVector> generator = createGenerator();
		while (generator.hasNext()) {
			InputVector vector = generator.next();
			EncodingUtils.write(vector.toIntArray(), out);
		}
		out.close();
	}

	public Iterator<InputVector> createGenerator() {
		return new InputVectorGenerator(settings);
	}

	public Iterator<InputVector> createFileIterator() {
		return new InputVectorFileIterator(benchmarkSettings.getInputVectorFilePath(),
				settings.getInputVectorSize());
	}

	/**
	 * @param args
	 *            path to settings
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Settings settings = new Settings();
		settings.load(args[0]);
		
		BenchmarkSettings benchmarkSettings = new BenchmarkSettings();
		benchmarkSettings.load(args[1]);
		new InputVectorSource(settings, benchmarkSettings).generateVectors();
	}

	private static class InputVectorFileIterator implements
			Iterator<InputVector> {

		public static final String PK_READ_VECTORS = "Read input vectors";

		private InputVector next;
		private InputStream in;
		private int inputVectorSize;
		private byte[] buffer;

		public InputVectorFileIterator(String file, int inputVectorSize) {
			try {
				this.in = new BufferedInputStream(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				throw new IllegalArgumentException("Invalid input vector file",
						e);
			}
			this.inputVectorSize = inputVectorSize;
			this.buffer = new byte[Integer.SIZE / 8 * inputVectorSize];
			move();
		}

		private void move() {
			Profiler.start(PK_READ_VECTORS);
			if (in != null) {
				try {
					int[] vector = new int[inputVectorSize];
					EncodingUtils.readCompleteArray(vector, buffer, in);
					next = new IntArrayInputVector(vector);
				} catch (IOException e) {
					try {
						in.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					next = null;
					in = null;
				}
			}
			Profiler.stop(PK_READ_VECTORS);
		}

		public boolean hasNext() {
			return next != null;
		}

		public InputVector next() {
			InputVector requestedNext = next;
			move();
			return requestedNext;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private class InputVectorGenerator implements Iterator<InputVector> {
		
		public static final String PK_GENERATE_VECTORS = "Generate vectors";

		private int numGeneratedVectors;
		private Settings settings;
		private InputVector baseVector;

		public InputVectorGenerator(Settings settings) {
			this.settings = settings;
		}

		public boolean hasNext() {
			return numGeneratedVectors < benchmarkSettings.getNumInputVectors();
		}

		public InputVector next() {
			numGeneratedVectors++;
			if (baseVector == null || Math.random() > 0.99d) {
			
				Profiler.start(PK_GENERATE_VECTORS);
				baseVector = IntArrayInputVector
						.generateInputRandomVector(settings.getInputVectorSize(), 
						    benchmarkSettings.getVectorComponentRange());
				Profiler.stop(PK_GENERATE_VECTORS);
				
				return baseVector;
			}

			Profiler.start(PK_GENERATE_VECTORS);
			InputVector mutatedVector = IntArrayInputVector.mutateVector(baseVector,
			    benchmarkSettings.getVectorComponentRange(),
					Math.pow(Math.random(), 4));
			Profiler.stop(PK_GENERATE_VECTORS);
			
			return mutatedVector;

		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

}
