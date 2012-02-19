/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2011  AO Industries, Inc.
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
package com.aoindustries.sql;

import java.sql.Timestamp;

/**
 * An unmodifiable Timestamp that may be used in return values and shared between threads.
 *
 * @author  AO Industries, Inc.
 */
final public class UnmodifiableTimestamp extends Timestamp {

    private static final long serialVersionUID = 1L;

    public UnmodifiableTimestamp(long time) {
        super(time);
    }

    /**
     * @deprecated instead use the constructor <code>Timestamp(long millis)</code>
     */
    @Deprecated
    public UnmodifiableTimestamp(int year, int month, int date, int hour, int minute, int second, int nano) {
        super(year, month, date, hour, minute, second, nano);
    }

    /**
     * @deprecated As of JDK version 1.1,
     */
    @Deprecated
    @Override
    public void setYear(int year) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated As of JDK version 1.1,
     */
    @Deprecated
    @Override
    public void setMonth(int month) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated As of JDK version 1.1,
     */
    @Deprecated
    @Override
    public void setDate(int month) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated As of JDK version 1.1,
     */
    @Deprecated
    @Override
    public void setHours(int month) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated As of JDK version 1.1,
     */
    @Deprecated
    @Override
    public void setMinutes(int month) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated As of JDK version 1.1,
     */
    @Deprecated
    @Override
    public void setSeconds(int month) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTime(long time) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNanos(int n) {
        throw new UnsupportedOperationException();
    }
}