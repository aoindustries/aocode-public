/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2010, 2011, 2013  AO Industries, Inc.
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
package com.aoindustries.util;

import com.aoindustries.util.AoCollections.PeekIterator;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.PriorityQueue;
// import org.checkthread.annotations.ThreadSafe;

/**
 * General-purpose array utilities and constants.
 *
 * @author  AO Industries, Inc.
 */
public class AoArrays {

    private AoArrays() {
    }

    public static final Serializable[] EMPTY_SERIALIZABLE_ARRAY = {};

    /**
     * Checks if the subrange of two byte arrays is equal.
     */
    // @ThreadSafe
    public static boolean equals(byte[] b1, byte[] b2, int off, int len) {
        for(int end=off+len; off<end; off++) {
            if(b1[off]!=b2[off]) return false;
        }
        return true;
    }

    /**
     * Checks if the subrange of two byte arrays is equal.
     */
    // @ThreadSafe
    public static boolean equals(byte[] b1, int off1, byte[] b2, int off2, int len) {
        for(int end=off1+len; off1<end; off1++, off2++) {
            if(b1[off1]!=b2[off2]) return false;
        }
        return true;
    }

    /**
     * Checks if all the values in the provided range are equal to <code>value</code>.
     */
    // @ThreadSafe
    public static boolean allEquals(byte[] b, int off, int len, byte value) {
        for(int end=off+len; off<end; off++) {
            if(b[off]!=value) return false;
        }
        return true;
    }

    /**
     * Compares two byte[].  Shorter byte[] are ordered before longer when
     * the shorter is a prefix of the longer.  The comparison considers each
     * byte as a value from 0-255.
     */
    // @ThreadSafe
    public static int compare(byte[] ba1, byte[] ba2) {
        int len = Math.min(ba1.length, ba2.length);
        for(int i=0; i<len; i++) {
            int b1 = ba1[i]&255;
            int b2 = ba2[i]&255;
            if(b1<b2) return -1;
            if(b1>b2) return 1;
        }
        if(ba1.length>len) return 1;
        if(ba2.length>len) return -1;
        return 0;
    }

    /**
     * Merges multiple already-sorted collections into one big array.
     *
     * Worst-cast Complexity:
     * 
     *     0 collections: constant
     *
     *     1 collection: O(n), where n is the number of elements in the collection
     *
     *     2 collection: O(n+m), where n is the number of elements in one collection, and m is the number of elements in the other collection
     *
     *     3+ collections: O(n*log(m)), where n is the total number of elements in all collections, and m is the number of collections
     *
     * @return Object[] of results.
     */
    @SuppressWarnings("unchecked")
    public static <V> V[] merge(Class<V> clazz, Collection<? extends Collection<? extends V>> collections, final Comparator<? super V> comparator) {
        final int numCollections = collections.size();
        // Zero is easy
        if(numCollections==0) return (V[])Array.newInstance(clazz, 0);
        // One collection - just use toArray
        else if(numCollections == 1) {
            Collection<? extends V> collection = collections.iterator().next();
            return collection.toArray((V[])Array.newInstance(clazz, collection.size()));
        }
        // Two collections - use very simple merge
        else if(numCollections == 2) {
            Iterator<? extends Collection<? extends V>> collIter = collections.iterator();
            final Collection<? extends V> c1 = collIter.next();
            final Collection<? extends V> c2 = collIter.next();
            assert !collIter.hasNext();
            final Iterator<? extends V> i1 = c1.iterator();
            final Iterator<? extends V> i2 = c2.iterator();
            final int totalSize = c1.size() + c2.size();

            @SuppressWarnings("unchecked")
            final V[] results = (V[])Array.newInstance(clazz, totalSize);
            V next1 = i1.hasNext() ? i1.next() : null;
            V next2 = i2.hasNext() ? i2.next() : null;
            int pos = 0;
            while(true) {
                if(next1==null) {
                    if(next2==null) {
                        // Both done
                        break;
                    } else {
                        // Get rest of i2
                        results[pos++] = next2;
                        while(i2.hasNext()) results[pos++] = i2.next();
                        break;
                    }
                } else {
                    if(next2==null) {
                        // Get rest of i1
                        results[pos++] = next1;
                        while(i1.hasNext()) results[pos++] = i1.next();
                        break;
                    } else {
                        if(comparator.compare(next1, next2)<=0) {
                            results[pos++] = next1;
                            next1 = i1.hasNext() ? i1.next() : null;
                        } else {
                            results[pos++] = next2;
                            next2 = i2.hasNext() ? i2.next() : null;
                        }
                    }
                }
            }
            if(pos!=totalSize) throw new ConcurrentModificationException();
            return results;
        } else {
            // 3+ collections, use priority queue
            PriorityQueue<AoCollections.PeekIterator<? extends V>> pq = new PriorityQueue<>(
                numCollections,
                new Comparator<AoCollections.PeekIterator<? extends V>>() {
                    @Override
                    public int compare(PeekIterator<? extends V> i1, PeekIterator<? extends V> i2) {
                        return comparator.compare(i1.peek(), i2.peek());
                    }
                }
            );
            int totalSize = 0;
            for(Collection<? extends V> collection : collections) {
                pq.add(AoCollections.peekIterator(collection.iterator()));
                totalSize += collection.size();
            }
            @SuppressWarnings("unchecked")
            final V[] results = (V[])Array.newInstance(clazz, totalSize);
            int pos = 0;
            PeekIterator<? extends V> pi;
            while((pi=pq.poll())!=null) {
                results[pos++] = pi.next();
                if(pi.hasNext()) pq.offer(pi);
            }
            if(pos!=totalSize) throw new ConcurrentModificationException();
            return results;
        }
    }
}
