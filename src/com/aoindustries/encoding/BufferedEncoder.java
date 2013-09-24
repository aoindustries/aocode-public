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
package com.aoindustries.encoding;

import java.io.IOException;
import java.io.Writer;

/**
 * Buffers the content to perform final output on writeSuffix.
 *
 * @author  AO Industries, Inc.
 */
abstract public class BufferedEncoder extends MediaEncoder {

    /**
     * Buffers all contents to pass to writeSuffix.
     */
    private final StringBuilder buffer;

	protected BufferedEncoder(int initialCapacity) {
		this.buffer = new StringBuilder(initialCapacity);
    }

    @Override
    final public void write(int c, Writer out) throws IOException {
        buffer.append((char)c);
    }

	@Override
    final public void write(char cbuf[], Writer out) throws IOException {
        buffer.append(cbuf);
	}

	@Override
    final public void write(char[] cbuf, int off, int len, Writer out) throws IOException {
        buffer.append(cbuf, off, len);
    }

	@Override
    final public void write(String str, Writer out) throws IOException {
        if(str==null) throw new IllegalArgumentException("str is null");
        buffer.append(str);
	}

	@Override
    final public void write(String str, int off, int len, Writer out) throws IOException {
        if(str==null) throw new IllegalArgumentException("str is null");
        buffer.append(str, off, off+len);
    }

    @Override
    final public BufferedEncoder append(char c, Appendable out) throws IOException {
        buffer.append(c);
        return this;
    }

	@Override
    final public BufferedEncoder append(CharSequence csq, Appendable out) throws IOException {
		buffer.append(csq);
        return this;
    }

    @Override
    final public BufferedEncoder append(CharSequence csq, int start, int end, Appendable out) throws IOException {
		buffer.append(csq, start, end);
        return this;
    }

	/**
	 * Writes the suffix and clears the buffer for reuse.
	 */
	@Override
    final public void writeSuffix(Appendable out) throws IOException {
		writeSuffix(buffer, out);
		buffer.setLength(0);
    }

	abstract protected void writeSuffix(StringBuilder buffer, Appendable out) throws IOException;
}