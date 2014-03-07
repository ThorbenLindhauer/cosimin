package de.unipotsdam.hpi.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Iterator;

import de.unipotsdam.hpi.indexing.IndexPair;
import de.unipotsdam.hpi.util.BitSignatureUtil;
import de.unipotsdam.hpi.util.EncodingUtils;

public class BitSignatureDiskStorage implements BitSignatureStorage {

	private Path path;
	private BufferedOutputStream out;
	private int signatureLen;
	private boolean appendToExistingStorage;

	public BitSignatureDiskStorage(Path path, int signatureLen, boolean appendToExistingStorage) {
		this.path = path;
		this.signatureLen = signatureLen;
		this.appendToExistingStorage = appendToExistingStorage;
	}

	synchronized public void store(IndexPair indexPair) {
		ensureOutputStreamOpen();
		try {
			EncodingUtils.writeLongArray(indexPair.getBitSignature(), out);
			EncodingUtils.writeInt(indexPair.getElementId(), out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void ensureOutputStreamOpen() {
		if (out == null) {
			try {
				FileOutputStream fileStream = new FileOutputStream(
						path.toFile(), appendToExistingStorage);
				out = new BufferedOutputStream(fileStream);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void closeOutput() {
		if (out != null) {
			try {
				out.close();
				out = null;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void flushOutput() {
		if (out != null) {
			try {
				out.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void clear() {
		closeOutput();
		path.toFile().delete();
	}

	public Iterator<IndexPair> iterator() {
		closeOutput();
		return new StorageIterator();
	}

	private class StorageIterator implements Iterator<IndexPair> {

		private InputStream in;
		private IndexPair next;
		private byte[] buffer;

		public StorageIterator() {
			try {
				in = new BufferedInputStream(new FileInputStream(path.toFile()));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
			move();
		}

		public boolean hasNext() {
			return next != null;
		}

		public IndexPair next() {
			IndexPair returnValue = next;
			move();
			return returnValue;
		}

		private void move() {
			long[] signature = new long[signatureLen >> BitSignatureUtil.LOG_BASE_TYPE_SIZE];
			if (buffer == null) {
				buffer = new byte[signatureLen / Byte.SIZE];
			}
			try {
				EncodingUtils.readCompleteArray(signature, buffer, in);
				int id = EncodingUtils.readInt(in);
				next = new IndexPair(signature, id);
			} catch (EOFException e) {
				next = null;
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				closeIfFinished();
			}
		}

		private void closeIfFinished() {
			if (next == null) {
				try {
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	};
}
