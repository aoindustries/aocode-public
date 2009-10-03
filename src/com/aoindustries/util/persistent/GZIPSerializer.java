/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2009  AO Industries, Inc.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Wraps a serializer and compresses the data using GZIP.
 * This class is not thread safe.
 *
 * @author  AO Industries, Inc.
 */
public class GZIPSerializer<E> implements Serializer<E> {

    private final Serializer<E> wrapped;

    private E lastSerialized = null;
    final private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public GZIPSerializer(Serializer<E> wrapped) {
        this.wrapped = wrapped;
    }

    private void serializeToBuffer(E value) throws IOException {
        if(lastSerialized!=value) {
            lastSerialized = null;
            buffer.reset();
            GZIPOutputStream gzout = new GZIPOutputStream(buffer);
            try {
                wrapped.serialize(value, gzout);
            } finally {
                gzout.close();
            }
            lastSerialized = value;
        }
    }

    public boolean isFixedSerializedSize() {
        return false;
    }

    public long getSerializedSize(E value) throws IOException {
        serializeToBuffer(value);
        return buffer.size();
    }

    public void serialize(E value, OutputStream out) throws IOException {
        buffer.writeTo(out);
    }

    public E deserialize(InputStream in) throws IOException {
        GZIPInputStream gzin = new GZIPInputStream(in);
        try {
            return wrapped.deserialize(gzin);
        } finally {
            gzin.close();
        }
    }
}