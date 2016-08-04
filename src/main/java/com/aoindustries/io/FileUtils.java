/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2011, 2012, 2013, 2014, 2015, 2016  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aocode-public.
 *
 * aocode-public is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aocode-public is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aocode-public.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;

/**
 * File utilities.
 */
final public class FileUtils {

	/**
	 * Make no instances.
	 */
	private FileUtils() {}

	/**
	 * Deletes the provided file, throwing IOException if unsuccessful.
	 */
	public static void delete(File file) throws IOException {
		if(!file.delete()) throw new IOException("Unable to delete: " + file);
	}

	/**
	 * Recursively deletes the provided file, being careful to not follow symbolic links (but there are still unavoidable race conditions).
	 */
	public static void deleteRecursive(File file) throws IOException {
		if(
			file.isDirectory()
			// Don't recursively travel across symbolic links (still a race condition here, though)
			&& file.getCanonicalPath().equals(file.getAbsolutePath())
		) {
			File afile[] = file.listFiles();
			if(afile != null) for(File f : afile) deleteRecursive(f);
		}
		delete(file);
	}

	/**
	 * Compares the contents of a file to the provided array.
	 */
	public static boolean contentEquals(File file, byte[] contents) throws IOException {
		{
			final long length = file.length();
			if(length>Integer.MAX_VALUE) return false;
			// Be careful about file.length() returning zero on error - always read file for zero case - no shortcut.
			if(length!=0 && length!=contents.length) return false;
		}
		try (InputStream in = new FileInputStream(file)) {
			return IoUtils.contentEquals(in, contents);
		}
	}

	/**
	 * Compares the contents of two files, not supporting directories.
	 */
	public static boolean contentEquals(File file1, File file2) throws IOException {
		long len1 = file1.length();
		long len2 = file2.length();
		// Read file when zero length
		if(len1!=0 && len2!=0 && len1!=len2) return false;
		try (
			InputStream in1 = new BufferedInputStream(new FileInputStream(file1));
			InputStream in2 = new BufferedInputStream(new FileInputStream(file2))
		) {
			while(true) {
				int b1 = in1.read();
				int b2 = in2.read();
				if(b1!=b2) return false;
				if(b1==-1) break;
			}
			return true;
		}
	}

	/**
	 * Computes a hash code of the file contents, consistent with Arrays.hashCode(byte[]).
	 * 
	 * @see  Arrays#hashCode(byte[])
	 */
	public static int contentHashCode(File file) throws IOException {
		try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
			int result = 1;
			int readVal;
			while((readVal = in.read()) != -1) {
				result = 31 * result + (byte)readVal;
			}
			return result;
		}
	}

	/**
	 * Creates a temporary directory.
	 */
	public static File createTempDirectory(String prefix, String suffix) throws IOException {
		return createTempDirectory(prefix, suffix, null);
	}

	/**
	 * Creates a temporary directory.
	 */
	public static File createTempDirectory(String prefix, String suffix, File directory) throws IOException {
		while(true) {
			File tempFile = File.createTempFile(prefix, suffix, directory);
			delete(tempFile);
			// Check result of mkdir to catch race condition
			if(tempFile.mkdir()) return tempFile;
		}
	}

	/**
	 * Copies a stream to a newly created temporary file.
	 */
	public static File copyToTempFile(InputStream in, String prefix, String suffix) throws IOException {
		return copyToTempFile(in, prefix, suffix, null);
	}

	/**
	 * Copies a stream to a newly created temporary file.
	 */
	public static File copyToTempFile(InputStream in, String prefix, String suffix, File directory) throws IOException {
		File tmpFile = File.createTempFile("cache_", null);
		boolean successful = false;
		try {
			try (OutputStream out = new FileOutputStream(tmpFile)) {
				IoUtils.copy(in, out);
			}
			successful = true;
			return tmpFile;
		} finally {
			if(!successful) delete(tmpFile);
		}
	}

	/**
	 * Makes a directory.  The directory must not already exist.
	 *
	 * @return  The directory itself.
	 *
	 * @exception  IOException  if mkdir fails.
	 */
	public static File mkdir(File directory) throws IOException {
		if(!directory.mkdir()) throw new IOException("Unable to create directory: "+directory.getPath());
		return directory;
	}

	/**
	 * Makes a directory and all of its parents.  The directory must not already exist.
	 *
	 * @return  The directory itself.
	 *
	 * @exception  IOException  if mkdirs fails.
	 */
	public static File mkdirs(File directory) throws IOException {
		if(!directory.mkdirs()) throw new IOException("Unable to create directory or one of its parents: "+directory.getPath());
		return directory;
	}

	/**
	 * Ensures that the file is a directory.
	 *
	 * @return  The directory itself.
	 *
	 * @exception  IOException  if not a directory
	 */
	public static File checkIsDirectory(File directory) throws IOException {
		if(!directory.isDirectory()) throw new IOException("Not a directory: " + directory.getPath());
		return directory;
	}

	/**
	 * Copies one file over another, possibly creating if needed.
	 *
	 * @return  the number of bytes copied
	 */
	public static long copy(File from, File to) throws IOException {
		try (InputStream in = new FileInputStream(from)) {
			long bytes;
			long modified = from.lastModified();
			try (OutputStream out = new FileOutputStream(to)) {
				bytes = IoUtils.copy(in, out);
			}
			if(modified!=0) to.setLastModified(modified);
			return bytes;
		}
	}

	/**
	 * Copies a file to an output stream.
	 *
	 * @return  the number of bytes copied
	 */
	public static long copy(File from, OutputStream out) throws IOException {
		try (InputStream in = new FileInputStream(from)) {
			return IoUtils.copy(in, out);
		}
	}

	/**
	 * Copies a file to a writer in system default locale.
	 *
	 * @return  the number of characters copied
	 */
	public static long copy(File from, Writer out) throws IOException {
		try (Reader in = new FileReader(from)) {
			return IoUtils.copy(in, out);
		}
	}

	/**
	 * Copies a file to an appendable in system default locale.
	 *
	 * @return  the number of characters copied
	 */
	public static long copy(File from, Appendable out) throws IOException {
		try (Reader in = new FileReader(from)) {
			return IoUtils.copy(in, out);
		}
	}

	/**
	 * Recursively copies source to destination.  Destination must not exist.
	 */
	public static void copyRecursive(File from, File to) throws IOException {
		copyRecursive(from, to, null);
	}

	/**
	 * Recursively copies source to destination.  Destination must not exist.
	 */
	public static void copyRecursive(File from, File to, FileFilter fileFilter) throws IOException {
		if(fileFilter==null || fileFilter.accept(from)) {
			if(from.isDirectory()) {
				if(to.exists()) throw new IOException("Directory exists: "+to);
				long modified = from.lastModified();
				mkdir(to);
				String[] list = from.list();
				if(list!=null) {
					for(String child : list) {
						copyRecursive(
							new File(from, child),
							new File(to, child),
							fileFilter
						);
					}
				}
				if(modified!=0) to.setLastModified(modified);
			} else if(from.isFile()) {
				if(to.exists()) throw new IOException("File exists: "+to);
				copy(from, to);
			} else {
				throw new IOException("Neither directory not file: "+to);
			}
		}
	}

	/**
	 * Gets the extension from the path, not including any period.
	 * If no extension, returns an empty string.
	 */
	public static String getExtension(String path) {
		int pos=path.lastIndexOf('.');
		if(pos<1) return "";
		// If a / follows the ., then no extension
		int pos2=path.indexOf('/', pos+1);
		if(pos2!=-1) return "";
		return path.substring(pos+1);
	}

	/**
	 * Gets a File for a URL, retrieving the contents into a temporary file if needed.
	 *
	 * @param  deleteOnExit  when <code>true</code>, any newly created temp file will be flagged for deleteRecursive of exit
	 */
	public static File getFile(URL url, String urlEncoding, boolean deleteOnExit) throws IOException {
		if("file".equalsIgnoreCase(url.getProtocol())) {
			String path = url.getFile();
			if(path.length()>0) {
				File file = new File(URLDecoder.decode(path, urlEncoding).replace('/', File.separatorChar));
				if(file.exists() && file.isFile()) return file;
			}
		}
		File file = File.createTempFile("url", null);
		boolean successful = false;
		try {
			if(deleteOnExit) file.deleteOnExit();
			try (
				OutputStream out = new FileOutputStream(file);
				InputStream in = url.openStream()
			) {
				IoUtils.copy(in, out);
			}
			successful = true;
			return file;
		} finally {
			if(!successful) delete(file);
		}
	}

	/**
	 * Atomically renames one file to another, throwing IOException when unsuccessful.
	 */
	public static void rename(File from, File to) throws IOException {
		if(!from.renameTo(to)) throw new IOException("Unable to atomically rename \""+from+"\" to \""+to+'"');
	}

	/**
	 * Renames one file to another, throwing IOException when unsuccessful.
	 * Allow a non-atomic delete/rename pair when the underlying system is unable
	 * to rename one file over another, such as in Microsoft Windows.
	 * <p>
	 * When the delete or rename fails, will retry up to ten times with increasing
	 * delay between each attempt.
	 * </p>
	 */
	public static void renameAllowNonAtomic(File from, File to) throws IOException {
		// Try atomic rename first
		if(!from.renameTo(to)) {
			final int NUM_TRIES = 10;
			for(int attempt=1; attempt<=NUM_TRIES; attempt++) {
				// Try delete then rename for Windows
				if(
					(!to.exists() || to.delete())
					&& from.renameTo(to)
				) {
					break;
				}
				if(attempt == NUM_TRIES) throw new IOException("Unable to non-atomically rename \""+from+"\" to \""+to+'"');
				try {
					Thread.sleep(4 << NUM_TRIES);
				} catch(InterruptedException e2) {
					IOException ioErr = new InterruptedIOException();
					ioErr.initCause(e2);
					throw ioErr;
				}
			}
		}
	}

	/**
	 * Reads the contents of a File and returns as a String.
	 */
	public static String readFileAsString(File file) throws IOException {
		long len = file.length();
		StringBuilder SB = len>0 && len<=Integer.MAX_VALUE ? new StringBuilder((int)len) : new StringBuilder();
		try (BufferedReader in = new BufferedReader(new FileReader(file))) {
			int ch;
			while((ch=in.read())!=-1) SB.append((char)ch);
		}
		return SB.toString();
	}    
}