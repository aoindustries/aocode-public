/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2009, 2010, 2011, 2012  AO Industries, Inc.
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
package com.aoindustries.util.persistent;

import com.aoindustries.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;
// import org.checkthread.annotations.NotThreadSafe;

/**
 * Uses <code>RandomAccessFile</code> for persistence.  Obtains a shared lock
 * on the channel for read-only mode or an exclusive lock for write mode.  The
 * lock is held until the buffer is closed.
 *
 * @author  AO Industries, Inc.
 */
public class RandomAccessFileBuffer extends AbstractPersistentBuffer {

    private static final Logger logger = Logger.getLogger(RandomAccessFileBuffer.class.getName());

    private final File tempFile;
    private final RandomAccessFile raf;
    private final FileChannel channel;
    private boolean closed;

    /**
     * Creates a read-write buffer backed by a temporary file.  The protection level
     * is set to <code>NONE</code>.  The temporary file will be deleted when this
     * buffer is closed or on JVM shutdown.
     */
    public RandomAccessFileBuffer() throws IOException {
        super(ProtectionLevel.NONE);
        tempFile = File.createTempFile("RandomAccessFileBuffer", null);
        tempFile.deleteOnExit();
        raf = new RandomAccessFile(tempFile, "rw");
        channel = raf.getChannel();
        // Lock the file
        channel.lock(0L, Long.MAX_VALUE, false);
    }

    /**
     * Creates a read-write buffer with <code>BARRIER</code> protection level.
     */
    public RandomAccessFileBuffer(String name) throws IOException {
        this(new RandomAccessFile(name, "rw"), ProtectionLevel.BARRIER);
    }

    /**
     * Creates a buffer.
     */
    public RandomAccessFileBuffer(String name, ProtectionLevel protectionLevel) throws IOException {
        this(new RandomAccessFile(name, protectionLevel==ProtectionLevel.READ_ONLY ? "r" : "rw"), protectionLevel);
    }

    /**
     * Creates a read-write buffer with <code>BARRIER</code> protection level.
     */
    public RandomAccessFileBuffer(File file) throws IOException {
        this(new RandomAccessFile(file, "rw"), ProtectionLevel.BARRIER);
    }

    /**
     * Creates a buffer.
     */
    public RandomAccessFileBuffer(File file, ProtectionLevel protectionLevel) throws IOException {
        this(new RandomAccessFile(file, protectionLevel==ProtectionLevel.READ_ONLY ? "r" : "rw"), protectionLevel);
    }

    /**
     * Creates a buffer using the provided <code>RandomAccessFile</code>.
     */
    public RandomAccessFileBuffer(RandomAccessFile raf, ProtectionLevel protectionLevel) throws IOException {
        super(protectionLevel);
        this.tempFile = null;
        this.raf = raf;
        channel = raf.getChannel();
        // Lock the file
        channel.lock(0L, Long.MAX_VALUE, protectionLevel==ProtectionLevel.READ_ONLY);
    }

    // @NotThreadSafe // closed field is not volatile
    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    // @NotThreadSafe
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    // @NotThreadSafe
    @Override
    public void close() throws IOException {
        closed = true;
        raf.close();
        if(tempFile!=null && tempFile.exists()) FileUtils.delete(tempFile);
    }

    // @NotThreadSafe
    @Override
    public long capacity() throws IOException {
        return raf.length();
    }

    // @NotThreadSafe
    @Override
    public void setCapacity(long newLength) throws IOException {
        long oldLength = capacity();
        raf.setLength(newLength);
        if(newLength>oldLength) {
            // Ensure zero-filled
            ensureZeros(oldLength, newLength - oldLength);
        }
    }

    // @NotThreadSafe
    @Override
    public int getSome(long position, byte[] buff, int off, int len) throws IOException {
        raf.seek(position);
        int count = raf.read(buff, off, len);
        if(count<0) throw new BufferUnderflowException();
        return count;
    }

    /**
     * Gets a single byte from the buffer.
     */
    @Override
    // @NotThreadSafe
    public byte get(long position) throws IOException {
        raf.seek(position);
        return raf.readByte();
    }

    @Override
    public void ensureZeros(long position, long len) throws IOException {
        PersistentCollections.ensureZeros(raf, position, len);
    }


    /**
     * Puts a single byte in the buffer.
     */
    @Override
    // @NotThreadSafe
    public void put(long position, byte value) throws IOException {
        if(position>=capacity()) throw new BufferOverflowException();
        raf.seek(position);
        raf.write(value);
    }

    // @NotThreadSafe
    @Override
    public void put(long position, byte[] buff, int off, int len) throws IOException {
        if((position+len)>capacity()) throw new BufferOverflowException();
        raf.seek(position);
        raf.write(buff, off, len);
    }

    /**
     * There is not currently a way to provide a barrier without using <code>force</code>.
     * This just uses force for each case.
     */
    // @NotThreadSafe
    @Override
    public void barrier(boolean force) throws IOException {
        if(protectionLevel.compareTo(ProtectionLevel.BARRIER)>=0) channel.force(false);
    }
}
