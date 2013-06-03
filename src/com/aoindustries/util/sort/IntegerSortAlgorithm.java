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
package com.aoindustries.util.sort;

/**
 * A sort implementation that sorts int[] primitives as was as integer representation of numeric objects.
 *
 * @author  AO Industries, Inc.
 */
abstract public class IntegerSortAlgorithm extends SortAlgorithm<Number> {

	protected IntegerSortAlgorithm() {
	}

    public void sort(int[] array) {
        sort(array, null);
    }

    public abstract void sort(int[] array, SortStatistics stats);

	protected static int get(int[] array, int i, SortStatistics stats) {
		if(stats!=null) stats.sortGetting();
		return array[i];
	}

	protected static void set(int[] array, int i, int value, SortStatistics stats) {
		if(stats!=null) stats.sortSetting();
		array[i]=value;
	}

	protected static void swap(int[] array, int i, int j, SortStatistics stats) {
		if(stats!=null) stats.sortSwapping();

		int T=array[i];
		array[i]=array[j];
		array[j]=T;
	}
}