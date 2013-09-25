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

import com.aoindustries.encoding.MediaEncoder;
import com.aoindustries.io.AoCharArrayWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * {@inheritDoc}
 *
 * This class is not thread safe.
 *
 * @author  AO Industries, Inc.
 */
public class CharArrayBufferResult implements BufferResult {

	/**
	 * @see  CharArrayBufferWriter#buffer
	 */
    private final AoCharArrayWriter buffer;

	private final int start;
	private final int end;

	protected CharArrayBufferResult(
		AoCharArrayWriter buffer,
		int start,
		int end
	) {
		this.buffer = buffer;
		this.start = start;
		this.end = end;
    }

	@Override
    public long getLength() {
        return end - start;
    }

	private String toStringCache;

    @Override
    public String toString() {
		if(toStringCache==null) toStringCache = buffer.toString(start, end - start);
		return toStringCache;
    }

	@Override
    public void writeTo(MediaEncoder encoder, Writer out) throws IOException {
		if(encoder==null) {
			writeTo(out);
		} else {
			encoder.write(
				buffer.getInternalCharArray(),
				start,
				end - start,
				out
			);
		}
	}

	@Override
    public void writeTo(Writer out) throws IOException {
		buffer.writeTo(out, start, end - start);
    }

	@Override
	public BufferResult trim() throws IOException {
		int newStart = this.start;
		final char[] buf = buffer.getInternalCharArray();
		// Skip past the beginning whitespace characters
		while(newStart<end) {
			char ch = buf[newStart];
			if(ch>' ') break;
			newStart++;
		}
		// Skip past the ending whitespace characters
		int newEnd = end;
		while(newEnd>newStart) {
			char ch = buf[newEnd-1];
			if(ch>' ') break;
			newEnd--;
		}
		// Keep this object if already trimmed
		if(
			start==newStart
			&& end==newEnd
		) {
			return this;
		} else {
			// Check if empty
			if(newStart==newEnd) {
				return EmptyResult.getInstance();
			} else {
				// Otherwise, return new substring
				return new CharArrayBufferResult(
					buffer,
					newStart,
					newEnd
				);
			}
		}
	}
}