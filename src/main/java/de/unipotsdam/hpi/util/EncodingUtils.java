package de.unipotsdam.hpi.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EncodingUtils {
	
	public static void writeInt(int i, OutputStream out) throws IOException {
		out.write(0xff & (i >> 24));
		out.write(0xff & (i >> 16));
		out.write(0xff & (i >> 8));
		out.write(0xff & i);
	}
	
	public static void writeLong(long i, OutputStream out) throws IOException {
		out.write((int) (0xff & (i >> 56)));
		out.write((int) (0xff & (i >> 48)));
		out.write((int) (0xff & (i >> 40)));
		out.write((int) (0xff & (i >> 32)));
		out.write((int) (0xff & (i >> 24)));
		out.write((int) (0xff & (i >> 16)));
		out.write((int) (0xff & (i >> 8)));
		out.write((int) (0xff & i));
	}
	
	public static void writeLongArray(long[] longs, OutputStream out) throws IOException {
		for (long l : longs)
			writeLong(l, out);
	}
	
	public static int readInt(InputStream in) throws IOException {
		int i = in.read();
		i = i << 8 | in.read();
		i = i << 8 | in.read();
		i = i << 8 | in.read();
		return i;
	}
	
	/**
	 *  Reads complete array from the input stream.
	 * 
	 * @throws EOFException if the stream is not long enough to fill the array
	 */
	public static void readCompleteArray(byte[] targetArray, InputStream in) throws IOException {
		int offset = 0;
		int readBytes = 0;
		while (offset < targetArray.length) {
			readBytes = in.read(targetArray, offset, targetArray.length - offset);
			if (readBytes == -1) throw new EOFException();
			offset += readBytes;
		}
	}
	
	public static void readCompleteArray(long[] signature, byte[] buffer, InputStream in) throws IOException {
		if (signature.length * 8 != buffer.length)
			throw new IllegalArgumentException("Buffer lengths are not according to each other");
		readCompleteArray(buffer, in);
		copy(buffer, signature);
	}

	public static void copy(byte[] src, long[] dest) {
		for (int i = 0, j = 0; i < dest.length; i++, j += 8) {
			dest[i] = 
					(((long) src[j]) << 56)
					| ((((long) src[j + 1]) & 0xFFL) << 48)
					| ((((long) src[j + 2]) & 0xFFL) << 40)
					| ((((long) src[j + 3]) & 0xFFL) << 32)
					| ((((long) src[j + 4]) & 0xFFL) << 24)
					| ((((long) src[j + 5]) & 0xFFL) << 16)
					| ((((long) src[j + 6]) & 0xFFL) << 8)
					| (((long) src[j + 7]) & 0xFFL);
		}
	}
	
	public static void copy(byte[] src, int[] dest) {
		for (int i = 0, j = 0; i < dest.length; i++, j += 4) {
			dest[i] = ((src[j] & 0xFF) << 24)
					| ((src[j + 1] & 0xFF) << 16)
					| ((src[j + 2] & 0xFF) << 8)
					| (src[j + 3] & 0xFF);
		}
	}

	public static void write(int[] intArray, OutputStream out) throws IOException {
		for (int component : intArray) {
			writeInt(component, out);
		}
	}

	public static void readCompleteArray(int[] intArray, byte[] buffer,
			InputStream in) throws IOException {
		if (intArray.length * 4 != buffer.length)
			throw new IllegalArgumentException("Buffer lengths are not according to each other");
		readCompleteArray(buffer, in);
		copy(buffer, intArray);
	}
}
