/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013  AO Industries, Inc.
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

import java.io.*;

/**
 * Provides direct access to the internal <code>byte[]</code>
 *
 * @author  AO Industries, Inc.
 */
public class AoByteArrayOutputStream extends ByteArrayOutputStream {

    public AoByteArrayOutputStream() {
        super();
    }

    public AoByteArrayOutputStream(int size) {
        super(size);
    }

	/**
	 * Provides direct access to the internal byte[] to avoid unnecessary
	 * copying of the array.
	 */
    public byte[] getInternalByteArray() {
        return this.buf;
    }

    /**
     * Writes a portion of the contents of the buffer to another byte stream.
     */
    public synchronized void writeTo(OutputStream out, int off, int len) throws IOException {
        out.write(buf, off, len);
    }

    public synchronized void writeTo(RandomAccessFile raf) throws IOException {
		raf.write(buf, 0, count);
    }

    public synchronized void writeTo(RandomAccessFile raf, int off, int len) throws IOException {
		raf.write(buf, off, len);
    }
}
