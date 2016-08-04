/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2012  AO Industries, Inc.
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
package com.aoindustries.math;

import java.math.BigInteger;

/**
 * Static utilities for dealing with long values as if they are unsigned.
 *
 * @author  AO Industries, Inc.
 */
public class UnsignedLong {

    private static final BigInteger TWO_POWER_64 = new BigInteger("10000000000000000", 16);
    private static final BigInteger TWO_POWER_64_MINUS_1 = TWO_POWER_64.subtract(BigInteger.ONE);

    /**
     * Make no instances.
     */
    private UnsignedLong() {
    }

    private static BigInteger getUnsigned(long value) {
        BigInteger bigValue = BigInteger.valueOf(value);
        if(value<0) {
            bigValue = bigValue.add(TWO_POWER_64).and(TWO_POWER_64_MINUS_1);
        }
        return bigValue;
    }

    /**
     * Divides two unsigned long values.
     */
    public static long divide(long dividend, long divisor) {
        // This may not be very fast, but it is simple to implement
        return getUnsigned(dividend).divide(getUnsigned(divisor)).longValue();
    }

    /**
     * Gets the remainder (modulus) from a division.
     */
    public static long remainder(long dividend, long divisor) {
        // This may not be very fast, but it is simple to implement
        return getUnsigned(dividend).remainder(getUnsigned(divisor)).longValue();
    }

    /**
     * Multiplies two unsigned long values.
     */
    /* Not necessary, regular multiplication should be OK
    public static long multiply(long factor1, long factor2) {
        // This may not be very fast, but it is simple to implement
        return getUnsigned(factor1).multiply(getUnsigned(factor2)).longValue();
    }
     */
}