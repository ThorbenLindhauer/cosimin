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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class EncodingUtilsTest {

	@Test
	public void testWriteIntToStream() throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(4);
		EncodingUtils.writeInt(42, outStream);
		byte[] outContents = outStream.toByteArray();

		Assert.assertArrayEquals(new byte[] { 0, 0, 0, 42 }, outContents);

		ByteArrayInputStream inStream = new ByteArrayInputStream(outContents);
		Assert.assertEquals(42, EncodingUtils.readInt(inStream));
	}

	@Test
	public void testWriteNegativeIntToStream() throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(4);
		EncodingUtils.writeInt(-123, outStream);
		byte[] outContents = outStream.toByteArray();

		Assert.assertArrayEquals(new byte[] { -1, -1, -1, -123 }, outContents);

		ByteArrayInputStream inStream = new ByteArrayInputStream(outContents);
		Assert.assertEquals(-123, EncodingUtils.readInt(inStream));
	}

	@Test
	public void testReadCompleteArray() throws IOException {
		byte[] source = new byte[12345];
		new Random().nextBytes(source);
		InputStream in = new LimitingByteArrayInputStream(source, 1234);
		byte[] sink = new byte[12345];
		EncodingUtils.readCompleteArray(sink, in);
		Assert.assertArrayEquals(source, sink);
	}

	@Test
	public void testReadCompleteArrayNotPossible() throws IOException {
		byte[] source = new byte[12345];
		new Random().nextBytes(source);
		InputStream in = new LimitingByteArrayInputStream(source, 1234);
		byte[] sink = new byte[12346];
		try {
			EncodingUtils.readCompleteArray(sink, in);
			Assert.fail("Expected exception.");
		} catch (EOFException e) {
			// happy path
		}
	}

	@Test
	public void testWriteLong() throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(4);
		EncodingUtils.writeLong(42, outStream);
		byte[] outContents = outStream.toByteArray();

		Assert.assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 42 },
				outContents);
	}

	@Test
	public void testWriteNegativeLong() throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(4);
		EncodingUtils.writeLong(-123, outStream);
		byte[] outContents = outStream.toByteArray();

		Assert.assertArrayEquals(
				new byte[] { -1, -1, -1, -1, -1, -1, -1, -123 }, outContents);
	}

	@Test
	public void testReadCompleteArray2() throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		EncodingUtils.writeLong(0, outStream);
		EncodingUtils.writeLong(1, outStream);
		EncodingUtils.writeLong(2, outStream);
		EncodingUtils.writeLong(3, outStream);
		byte[] outContents = outStream.toByteArray();

		InputStream in = new LimitingByteArrayInputStream(outContents, 1234);
		long[] readBuffer = new long[2];
		byte[] byteBuffer = new byte[8 * readBuffer.length];

		EncodingUtils.readCompleteArray(readBuffer, byteBuffer, in);
		Assert.assertArrayEquals(new long[] { 0,  1}, readBuffer);
		
		EncodingUtils.readCompleteArray(readBuffer, byteBuffer, in);
		Assert.assertArrayEquals(new long[] { 2,  3}, readBuffer);
		
		try {
			EncodingUtils.readCompleteArray(readBuffer, byteBuffer, in);
			Assert.fail("Exception expected.");
		} catch (IOException e) {
			// Happy path.
		}
	}

	private class LimitingByteArrayInputStream extends ByteArrayInputStream {

		private int maxBytesToRead;

		public LimitingByteArrayInputStream(byte[] array, int maxBytesToRead) {
			super(array);
			this.maxBytesToRead = maxBytesToRead;
		}

		@Override
		public synchronized int read(byte[] b, int off, int len) {
			return super.read(b, off, Math.min(len, maxBytesToRead));
		}

	}
}
