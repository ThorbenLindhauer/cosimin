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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class FileUtils {
  
  private static final Logger logger = Logger.getLogger(FileUtils.class.getName());

	public static void createDirectoryIfNotExists(Path directoryPath)
			throws IOException {
		Path path = directoryPath.toAbsolutePath();
		if (Files.notExists(path)) {
			createDirectoryIfNotExists(path.getParent());
			Files.createDirectory(path);
		}
	}

	public static Path[] getPaths(Path parentPath, int childDirectories,
			String childDirectoryPrefix) throws IOException {
		Path[] paths = new Path[childDirectories];
		for (int i = 0; i < childDirectories; i++) {
			Path childPath = parentPath.resolve(childDirectoryPrefix + i);
			createDirectoryIfNotExists(childPath);
			paths[i] = childPath;
		}

		return paths;
	}
	
	public static void clearAndDeleteDirecotry(Path path) {
	  clearDirectory(path);
	  path.toFile().delete();
	}

	public static void clearDirectory(Path path) {
		File file = path.toFile();
		if (!file.exists() || !file.isDirectory()) {
			System.err.println("FileUtils.clearDirectory: " + file
					+ " is not a valid directory.");
		} else {
			deleteChildren(file);
		}
	}

	private static void deleteChildren(File file) {
		for (File child : file.listFiles()) {
			if (child.isDirectory()) {
				deleteChildren(child);
			}
			child.delete();
		}
	}

	public static ObjectOutputStream createObjectOutputStreamTo(String path) {
		try {
			return new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(path, false)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void save(Object o, String path) {
		ObjectOutputStream stream = createObjectOutputStreamTo(path);
		try {
			stream.writeObject(o);
			stream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static ObjectInputStream createObjectInputStreamTo(String path) {
		try {
			return new ObjectInputStream(new BufferedInputStream(
					new FileInputStream(path)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Path toPath(String first, String... more) {
		return FileSystems.getDefault().getPath(first, more);
	}

	public static void save(Object o, Path path) {
		save(o, path.toString());
	}

	public static Object load(Path path) throws IOException {
		return load(path.toString());
	}

	public static Object load(String path) throws IOException {

		ObjectInputStream in = null;
		Object o = null;
		try {
			in = createObjectInputStreamTo(path);
			o = in.readObject();
		} catch (EOFException e) {
		  logger.warning("Warning: " + e.getMessage());
		} catch (FileNotFoundException e) {
		  logger.warning("Warning: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return o;
	}
}
