/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2014  AO Industries, Inc.
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
package com.aoindustries.messaging;

import com.aoindustries.util.AoArrays;

/**
 * Encapsulates a byte[] and the number of bytes used.
 */
public class ByteArray {

	public static final ByteArray EMPTY_BYTE_ARRAY = new ByteArray(AoArrays.EMPTY_BYTE_ARRAY);

	public final byte[] array;
	public final int size;

	public ByteArray(byte[] array) {
		this(array, array.length);
	}

	public ByteArray(byte[] array, int size) {
		this.array = array;
		this.size = size;
		assert(size <= array.length);
	}
}
