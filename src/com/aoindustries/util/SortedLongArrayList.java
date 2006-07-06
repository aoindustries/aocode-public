package com.aoindustries.util;

import java.util.Collection;
import java.util.Iterator;

/*
 * Copyright 2002-2006 by AO Industries, Inc.,
 * 816 Azalea Rd, Mobile, Alabama, 36693, U.S.A.
 * All rights reserved.
 */

/**
 * A <code>SortedLongArrayList</code> stores is elements in numerical order and provides means of quickly
 * locating objects.
 * 
 * @version  1.0
 * 
 * @author  AO Industries, Inc.
 */
public class SortedLongArrayList extends LongArrayList implements Cloneable, java.io.Serializable {

    private static final long serialVersionUID = -2587216177946775702L;

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param   initialCapacity   the initial capacity of the list.
     * @exception IllegalArgumentException if the specified initial capacity
     *            is negative
     */
    public SortedLongArrayList(int initialCapacity) {
	super(initialCapacity);
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public SortedLongArrayList() {
	super();
    }

    /**
     * Performs a binary search for the provide value.
     * It will return any matching element, not necessarily
     * the first or the last.
     */
    protected int binarySearch(long value) {
        int left = 0;
        int right = size-1;
        while(left <= right) {
            int mid = (left + right)>>1;
            long midValue = elementData[mid];
            if(value==midValue) return mid;
            if(value<midValue) right = mid-1;
            else left = mid+1;
        }
	return -(left+1);
    }

    /**
     * Searches for the first occurence of the given value. 
     *
     * @param   elem   the value
     * @return  the index of the first occurrence of the argument in this
     *          list; returns <tt>-1</tt> if the object is not found.
     */
    public int indexOf(long elem) {
        // Find the location to insert the object at
        int pos=binarySearch(elem);
        
        // Not found
        if(pos<0) return -1;

        // Found one, iterate backwards to the first one
        while(pos>0 && elementData[pos-1]==elem) pos--;
        return pos;
    }

    /**
     * Returns the index of the last occurrence of the specified object in
     * this list.
     *
     * @param   elem   the desired element.
     * @return  the index of the last occurrence of the specified object in
     *          this list; returns -1 if the object is not found.
     */
    public int lastIndexOf(long elem) {
        // Find the location to insert the object at
        int pos=binarySearch(elem);

        // Not found
        if(pos<0) return -1;

        // Found one, iterate forwards to the last one
        while(pos<(size-1) && elementData[pos+1]==elem) pos++;
        return pos;
    }

    /**
     * Not allowed to set specific indexes.
     */
    public long set(int index, long element) {
        throw new RuntimeException("Not allowed to set specific indexes");
    }

    /**
     * Adds the specified element in sorted position within this list.
     *
     * @param o element to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of Collection.add).
     */
    public boolean add(long o) {
        // Shortcut for empty
        int size=size();
        if(size==0) {
            super.add(o);
        } else {
            // Shortcut for adding to end (makes imports of already-sorted data operate at constant-time instead of logarithmic complexity)
            if(o>=elementData[size-1]) {
                super.add(o);
            } else {
                int index=binarySearch(o);
                if(index<0) {
                    // Not found in list
                    super.add(-(index+1), o);
                } else {
                    // Add after existing
                    super.add(index+1, o);
                }
            }
        }

        return true;
    }

    /**
     * Not allowed to add to specific indexes.
     */
    public void add(int index, long element) {
        throw new RuntimeException("Not allowed to add to specific indexes");
    }

    /**
     * Removes a single instance of the specified element from this
     * list, if it is present (optional operation).  More formally,
     * removes an element <tt>e</tt> such that <tt>(o==null ? e==null :
     * o.equals(e))</tt>, if the list contains one or more such
     * elements.  Returns <tt>true</tt> if the list contained the
     * specified element (or equivalently, if the list changed as a
     * result of the call).<p>
     *
     * @param value element to be removed from this list, if present.
     * @return <tt>true</tt> if the list contained the specified element.
     */
    public boolean removeByValue(long value) {
        int index=binarySearch(value);
        if(index<0) return false;
        removeLong(index);
        return true;
    }

    /**
     * Adds all of the elements in the specified Collection and sorts during
     * the add.  This may operate slowly as it is the same as individual
     * calls to the add method.
     */
    public boolean addAll(Collection<? extends Long> c) {
        Iterator<? extends Long> iter=c.iterator();
        boolean didOne=false;
        while(iter.hasNext()) {
            add(iter.next().longValue());
            didOne=true;
        }
	return didOne;
    }

    /**
     * Not allowed to add to a specific index.
     */
    public boolean addAll(int index, Collection<? extends Long> c) {
        throw new RuntimeException("Not allowed to add to a specific index");
    }
}
