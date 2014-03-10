package de.unipotsdam.hpi.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Iterator;

import de.unipotsdam.hpi.input.InputVector;
import de.unipotsdam.hpi.input.SparseInputVector;
import de.unipotsdam.hpi.util.EncodingUtils;
import de.unipotsdam.hpi.util.Profiler;

public class SparseInputVectorStorage {

  private Path path;
  private BufferedOutputStream out;

  public SparseInputVectorStorage(Path path) {
    this.path = path;
  }

  public void store(SparseInputVector vector) {
    ensureOutputStreamOpen();
    try {
      EncodingUtils.writeInt(vector.getId(), out);
      byte[] valuesAsBytes = vector.valuesToBytes();
      EncodingUtils.writeInt(valuesAsBytes.length, out);
      out.write(valuesAsBytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void ensureOutputStreamOpen() {
    if (out == null) {
      try {
        FileOutputStream fileStream = new FileOutputStream(path.toFile(), false);
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

  public Iterator<InputVector> createFileIterator() {
    return new InputVectorFileIterator(path);
  }

  private static class InputVectorFileIterator implements Iterator<InputVector> {

    public static final String PK_READ_VECTORS = "Read input vectors";

    private SparseInputVector next;
    private InputStream in;
//    private int inputVectorSize;
//    private byte[] buffer;

    public InputVectorFileIterator(Path filePath) {
      try {
        this.in = new BufferedInputStream(new FileInputStream(filePath.toFile()));
      } catch (FileNotFoundException e) {
        throw new IllegalArgumentException("Invalid input vector file", e);
      }
//      this.inputVectorSize = inputVectorSize;
//      this.buffer = new byte[Integer.SIZE / 8 * inputVectorSize];
      move();
    }

    private void move() {
      Profiler.start(PK_READ_VECTORS);
      if (in != null) {
        try {
          if (in.available() > 0) {
            int id = EncodingUtils.readInt(in);
            int valuesAsBytes = EncodingUtils.readInt(in);
            byte[] values = new byte[valuesAsBytes];
            in.read(values);
            next = SparseInputVector.fromBytes(id, values);
          } else {
            close();
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      Profiler.stop(PK_READ_VECTORS);
    }
    
    private void close() throws IOException {
      try {
        in.close();
      } catch (IOException e) {
        next = null;
        in = null;
        throw e;
      }
      next = null;
      in = null;
    }

    public boolean hasNext() {
      return next != null;
    }

    public SparseInputVector next() {
      SparseInputVector requestedNext = next;
      move();
      return requestedNext;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

  }
}
