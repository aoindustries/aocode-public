/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2008, 2011, 2013  AO Industries, Inc.
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

import static com.aoindustries.sql.ApplicationResources.accessor;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Encapsulates a time span in nanoseconds.
 *
 * @author  AO Industries, Inc.
 */
final public class NanoInterval implements Serializable, Comparable<NanoInterval> {

    private static final long serialVersionUID = 1;

    final private long intervalNanos;

    public NanoInterval(long intervalNanos) {
        this.intervalNanos = intervalNanos;
    }

    /**
     * Gets the interval in nanoseconds.
     */
    public long getIntervalNanos() {
        return intervalNanos;
    }

    @Override
    public String toString() {
        return toString(intervalNanos);
    }

    public static String toString(long intervalNanos) {
        if(intervalNanos < 1000000) return accessor.getMessage("NanoInterval.toString.micro", BigDecimal.valueOf(intervalNanos, 3));
        if(intervalNanos < 1000000000) return accessor.getMessage("NanoInterval.toString.milli", BigDecimal.valueOf(intervalNanos/1000, 3));
        return accessor.getMessage("NanoInterval.toString.second", BigDecimal.valueOf(intervalNanos/1000000, 3));
    }

	@Override
    public int compareTo(NanoInterval o) {
        if(intervalNanos<o.intervalNanos) return -1;
        if(intervalNanos>o.intervalNanos) return 1;
        return 0;
    }
}
