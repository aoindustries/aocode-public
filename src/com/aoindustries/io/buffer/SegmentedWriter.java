/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2013  AO Industries, Inc.
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
package com.aoindustries.io.buffer;

import com.aoindustries.io.TempFile;
import com.aoindustries.nio.charset.Charsets;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.channels.ClosedChannelException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Buffers all writes while switching to a temp file when the
 * threshold is reached.
 *
 * TODO: If writing to another segmented buffer, the segments will be shared between
 * the two instances.
 *
 * This class is not thread safe.
 * 
 * @see  AutoTempFileWriter  This draws heavily from experienced gained in <code>AutoTempFileWriter</code>, but with the addition of segments.
 *
 * @author  AO Industries, Inc.
 */
public class SegmentedWriter extends BufferWriter {

    private static final Logger logger = Logger.getLogger(SegmentedWriter.class.getName());

	/**
	 * The number of starting elements in segment arrays.
	 */
	private static final int START_LEN = 16;

	private static final byte[] EMPTY_BYTES = new byte[0];
	private static final Object[] EMPTY_OBJECTS = new Object[0];

	/**
	 * The set of internal types supported.
	 */
	static final byte
		TYPE_STRING = 1,
		TYPE_CHAR_NEWLINE = 2,
		TYPE_CHAR_QUOTE = 3,
		TYPE_CHAR_APOS = 4,
		TYPE_CHAR_OTHER = 5
	;

	static void writeSegment(byte type, Object value, Writer out) throws IOException {
		switch(type) {
			case TYPE_STRING :
				out.write((String)value);
				break;
			case TYPE_CHAR_NEWLINE :
				out.write('\n');
				break;
			case TYPE_CHAR_QUOTE :
				out.write('"');
				break;
			case TYPE_CHAR_APOS :
				out.write('\'');
				break;
			case TYPE_CHAR_OTHER :
				out.write(((Character)value).charValue());
				break;
			default :
				throw new AssertionError();
		}
	}

	/**
	 * Gets the length of a segment.
	 */
	static int getLength(byte type, Object value) {
		switch(type) {
			case SegmentedWriter.TYPE_STRING :
				return ((String)value).length();
			case SegmentedWriter.TYPE_CHAR_NEWLINE :
				return 1;
			case SegmentedWriter.TYPE_CHAR_QUOTE :
				return 1;
			case SegmentedWriter.TYPE_CHAR_APOS :
				return 1;
			case SegmentedWriter.TYPE_CHAR_OTHER :
				return 1;
			default :
				throw new AssertionError();
		}
	}

	private final int tempFileThreshold;

	/**
	 * The length of the writer is the sum of the length of all its segments.
	 * Once closed, this length will not be modified.
	 */
    private long length;

	/**
	 * The set of segments are maintained in an array.
	 * Once closed, these arrays will not be modified.
	 */
	private byte[] segmentTypes;
	private Object[] segmentValues;
	private int segmentCount;

	/**
	 * Once closed, no further information may be written.
	 * Manipulations are only active once closed.
	 */
	private boolean isClosed;

	// The temp file is in UTF16-BE encoding
    private TempFile tempFile;
    private Writer fileWriter;

	public SegmentedWriter(int tempFileThreshold) {
        this.tempFileThreshold = tempFileThreshold;
        this.length = 0;
		this.segmentTypes = EMPTY_BYTES;
		this.segmentValues = EMPTY_OBJECTS;
		this.segmentCount = 0;
		this.isClosed = false;
		this.tempFile = null;
		this.fileWriter = null;
    }

	private void switchIfNeeded(long newLength) throws IOException {
		final Object[] segs = this.segmentValues;
        if(segs!=null && newLength>=tempFileThreshold) {
            tempFile = new TempFile("SegmentedWriter"/*, null, new File("/dev/shm")*/);
            if(logger.isLoggable(Level.FINE)) logger.log(Level.FINE, "Switching to temp file: {0}", tempFile);
            fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile.getFile()), Charsets.UTF_16BE));
			// Write all segments to file
			final byte[] segTypes = this.segmentTypes;
			Writer out = fileWriter;
			for(int i=0, count=segmentCount; i<count; i++) {
				writeSegment(segTypes[i], segs[i], out);
			}
			this.segmentTypes = null;
            this.segmentValues = null;
			this.segmentCount = 0;
        }
    }

	/**
	 * Makes sure the segments have room for one more element.
	 */
	private void addSegment(byte type, Object value) {
		assert !isClosed;
		int len = segmentValues.length;
		if(segmentCount==len) {
			// Need to grow
			if(len==0) {
				this.segmentTypes = new byte[START_LEN];
				this.segmentValues = new Object[START_LEN];
			} else {
				// Double capacity and copy
				int newLen = len<<1;
				byte[] newTypes = new byte[newLen];
				System.arraycopy(segmentTypes, 0, newTypes, 0, len);
				Object[] newValues = new Object[newLen];
				System.arraycopy(segmentValues, 0, newValues, 0, len);
				this.segmentTypes = newTypes;
				this.segmentValues = newValues;
			}
		}
		segmentTypes[segmentCount] = type;
		segmentValues[segmentCount++] = value;
	}

	@Override
    public void write(int c) throws IOException {
		if(isClosed) throw new ClosedChannelException();
        long newLength = length + 1;
        switchIfNeeded(newLength);
        if(segmentValues!=null) {
			char ch = (char)c;
			switch(ch) {
				case '\n' :
					addSegment(TYPE_CHAR_NEWLINE, null);
					break;
				case '\'' :
					addSegment(TYPE_CHAR_APOS, null);
					break;
				case '"' :
					addSegment(TYPE_CHAR_QUOTE, null);
					break;
				default :
					addSegment(TYPE_CHAR_OTHER, Character.valueOf(ch));
			}
		} else {
	        fileWriter.write(c);
		}
        length = newLength;
    }

    @Override
    public void write(char cbuf[]) throws IOException {
		final int len = cbuf.length;
		if(len>0) {
			if(len==1) {
				write(cbuf[0]);
			} else {
				if(isClosed) throw new ClosedChannelException();
				long newLength = length + len;
				switchIfNeeded(newLength);
				if(segmentValues!=null) {
					addSegment(
						TYPE_STRING,
						String.copyValueOf(cbuf)
					);
				} else {
					fileWriter.write(cbuf);
				}
				length = newLength;
			}
		}
    }

    @Override
    public void write(char cbuf[], int off, int len) throws IOException {
		if(len>0) {
			if(len==1) {
				write(cbuf[off]);
			} else {
				if(isClosed) throw new ClosedChannelException();
				long newLength = length + len;
				switchIfNeeded(newLength);
				if(segmentValues!=null) {
					addSegment(
						TYPE_STRING,
						String.copyValueOf(cbuf, off, len)
					);
				} else {
					fileWriter.write(cbuf, off, len);
				}
				length = newLength;
			}
		}
    }

    @Override
    public void write(String str) throws IOException {
		final int len = str.length();
		if(len>0) {
			if(isClosed) throw new ClosedChannelException();
			long newLength = length + len;
			switchIfNeeded(newLength);
			if(segmentValues!=null) {
				if(len==1) {
					// Prefer character shortcuts
					switch(str.charAt(0)) {
						case '\n' :
							addSegment(TYPE_CHAR_NEWLINE, null);
							break;
						case '\'' :
							addSegment(TYPE_CHAR_APOS, null);
							break;
						case '"' :
							addSegment(TYPE_CHAR_QUOTE, null);
							break;
						default :
							addSegment(TYPE_STRING, str);
					}
				} else {
					addSegment(TYPE_STRING, str);
				}
			} else {
				fileWriter.write(str);
			}
			length = newLength;
		}
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
		if(len>0) {
			if(isClosed) throw new ClosedChannelException();
			long newLength = length + len;
			switchIfNeeded(newLength);
			if(segmentValues!=null) {
				if(len==1) {
					// Prefer character shortcuts
					switch(str.charAt(off)) {
						case '\n' :
							addSegment(TYPE_CHAR_NEWLINE, null);
							break;
						case '\'' :
							addSegment(TYPE_CHAR_APOS, null);
							break;
						case '"' :
							addSegment(TYPE_CHAR_QUOTE, null);
							break;
						default :
							addSegment(TYPE_STRING, str);
					}
				} else {
					addSegment(TYPE_STRING, str.substring(off, off+len));
				}
			} else {
				fileWriter.write(str, off, len);
			}
			length = newLength;
		}
    }

    @Override
    public SegmentedWriter append(CharSequence csq) throws IOException {
		if(csq==null) {
			write("null");
		} else {
			final int len = csq.length();
			if(len>0) {
				if(isClosed) throw new ClosedChannelException();
				long newLength = length + len;
				switchIfNeeded(newLength);
				if(segmentValues!=null) {
					if(len==1) {
						// Prefer character shortcuts
						switch(csq.charAt(0)) {
							case '\n' :
								addSegment(TYPE_CHAR_NEWLINE, null);
								break;
							case '\'' :
								addSegment(TYPE_CHAR_APOS, null);
								break;
							case '"' :
								addSegment(TYPE_CHAR_QUOTE, null);
								break;
							default :
								addSegment(TYPE_STRING, csq.toString());
						}
					} else {
						addSegment(TYPE_STRING, csq.toString());
					}
				} else {
					fileWriter.append(csq);
				}
				length = newLength;
			}
		}
		return this;
    }

    @Override
    public SegmentedWriter append(CharSequence csq, int start, int end) throws IOException {
		if(csq==null) {
			write("null");
		} else {
			final int len = end-start;
			if(len>0) {
				if(isClosed) throw new ClosedChannelException();
				long newLength = length + len;
				switchIfNeeded(newLength);
				if(segmentValues!=null) {
					if(len==1) {
						// Prefer character shortcuts
						switch(csq.charAt(start)) {
							case '\n' :
								addSegment(TYPE_CHAR_NEWLINE, null);
								break;
							case '\'' :
								addSegment(TYPE_CHAR_APOS, null);
								break;
							case '"' :
								addSegment(TYPE_CHAR_QUOTE, null);
								break;
							default :
								addSegment(TYPE_STRING, csq.subSequence(start, end).toString());
						}
					} else {
						addSegment(TYPE_STRING, csq.subSequence(start, end).toString());
					}
				} else {
					fileWriter.append(csq, start, end);
				}
				length = newLength;
			}
		}
        return this;
    }

    @Override
    public SegmentedWriter append(char c) throws IOException {
		if(isClosed) throw new ClosedChannelException();
        long newLength = length + 1;
        switchIfNeeded(newLength);
        if(segmentValues!=null) {
			switch(c) {
				case '\n' :
					addSegment(TYPE_CHAR_NEWLINE, null);
					break;
				case '\'' :
					addSegment(TYPE_CHAR_APOS, null);
					break;
				case '"' :
					addSegment(TYPE_CHAR_QUOTE, null);
					break;
				default :
					addSegment(TYPE_CHAR_OTHER, Character.valueOf(c));
			}
		} else {
			fileWriter.append(c);
		}
        length = newLength;
        return this;
    }

    @Override
    public void flush() throws IOException {
        if(fileWriter!=null) fileWriter.flush();
    }

    @Override
    public void close() throws IOException {
        if(fileWriter!=null) {
            fileWriter.close();
            fileWriter = null;
		}
		isClosed = true;
    }

	@Override
    public long getLength() {
        return length;
    }

    @Override
    public String toString() {
		return "SegmentedWriter(length=" + length + ")";
    }

	// The result is cached after first created
	private BufferResult result;

	@Override
	public BufferResult getResult() throws IllegalStateException {
		if(!isClosed) throw new IllegalStateException();
		if(result==null) {
			if(length==0) {
				result = EmptyResult.getInstance();
			} else if(segmentValues!=null) {
				int endSegmentIndex = segmentCount - 1;
				int endSegmentEnd =
					endSegmentIndex==-1
					? 0
					: getLength(
						segmentTypes[endSegmentIndex],
						segmentValues[endSegmentIndex]
					)
				;
				result = new SegmentedResult(
					length,
					segmentTypes,
					segmentValues,
					segmentCount,
					0,
					0,
					0,
					length,
					endSegmentIndex,
					endSegmentEnd
				);
			} else if(tempFile!=null) {
				result = new TempFileResult(tempFile, 0, length);
			} else {
				throw new AssertionError();
			}
		}
		return result;
	}
}