/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2009, 2010  AO Industries, Inc.
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
package com.aoindustries.util.i18n;

import java.util.Comparator;
import java.util.Locale;

/**
 * Sorts locales by language, country, then variant.
 *
 * @author  AO Industries, Inc.
 */
public class LocaleComparator implements Comparator<Locale> {

    private static final LocaleComparator instance = new LocaleComparator();

    /**
     * Singleton that may be shared.
     */
    public static LocaleComparator getInstance() {
        return instance;
    }

    private LocaleComparator() {
    }

    @Override
    public int compare(Locale l1, Locale l2) {
        int diff = l1.getLanguage().compareToIgnoreCase(l2.getLanguage());
        if(diff!=0) return diff;
        diff = l1.getCountry().compareToIgnoreCase(l2.getCountry());
        if(diff!=0) return diff;
        return l1.getVariant().compareToIgnoreCase(l2.getVariant());
    }
}
