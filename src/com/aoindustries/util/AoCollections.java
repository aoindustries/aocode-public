/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2010, 2011, 2012, 2013  AO Industries, Inc.
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

import com.aoindustries.lang.ObjectUtils;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * General-purpose collection utilities and constants.
 *
 * @author  AO Industries, Inc.
 */
public class AoCollections {

    private AoCollections() {
    }

    public static final SortedSet EMPTY_SORTED_SET = new EmptySortedSet();

    @SuppressWarnings("unchecked")
    public static final <T> SortedSet<T> emptySortedSet() {
        return (SortedSet<T>) EMPTY_SORTED_SET;
    }

    private static class EmptySortedSet extends AbstractSet<Object> implements SortedSet<Object>, Serializable {

        private static final long serialVersionUID = 5914343416838268017L;

        @Override
        public Iterator<Object> iterator() {
            return new Iterator<Object>() {
                @Override
                public boolean hasNext() {
                    return false;
                }
                @Override
                public Object next() {
                    throw new NoSuchElementException();
                }
                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public int size() {return 0;}

        @Override
        public boolean contains(Object obj) {
            return false;
        }

        private Object readResolve() {
            return EMPTY_SORTED_SET;
        }

        @Override
        public Comparator<? super Object> comparator() {
            return null;
        }

        @Override
        public SortedSet<Object> subSet(Object fromElement, Object toElement) {
            throw new IllegalArgumentException();
        }

        @Override
        public SortedSet<Object> headSet(Object toElement) {
            throw new IllegalArgumentException();
        }

        @Override
        public SortedSet<Object> tailSet(Object fromElement) {
            throw new IllegalArgumentException();
        }

        @Override
        public Object first() {
            throw new NoSuchElementException();
        }

        @Override
        public Object last() {
            throw new NoSuchElementException();
        }
    }

    public static <T> SortedSet<T> singletonSortedSet(T o) {
        return new SingletonSortedSet<T>(o);
    }

    private static class SingletonSortedSet<E> extends AbstractSet<E> implements SortedSet<E>, Serializable {

        private static final long serialVersionUID = -6732971044735913580L;

        final private E element;

        SingletonSortedSet(E e) {element = e;}

        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {
                private boolean hasNext = true;
                @Override
                public boolean hasNext() {
                    return hasNext;
                }
                @Override
                public E next() {
                    if (hasNext) {
                        hasNext = false;
                        return element;
                    }
                    throw new NoSuchElementException();
                }
                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean contains(Object o) {return ObjectUtils.equals(o, element);}

        @Override
        public Comparator<? super E> comparator() {
            return null;
        }

        @Override
        public SortedSet<E> subSet(E fromElement, E toElement) {
            if(ObjectUtils.equals(element, fromElement) && ObjectUtils.equals(element, toElement)) return emptySortedSet();
            throw new IllegalArgumentException();
        }

        @Override
        public SortedSet<E> headSet(E toElement) {
            if(ObjectUtils.equals(element, toElement)) return emptySortedSet();
            throw new IllegalArgumentException();
        }

        @Override
        public SortedSet<E> tailSet(E fromElement) {
            if(ObjectUtils.equals(element, fromElement)) return this;
            throw new IllegalArgumentException();
        }

        @Override
        public E first() {
            return element;
        }

        @Override
        public E last() {
            return element;
        }
    }

    private static final Class<?>[] unmodifiableCollectionClasses = {
        // Collection
        Collections.unmodifiableCollection(Collections.emptyList()).getClass(),

        // List
        Collections.singletonList(null).getClass(),
        Collections.unmodifiableList(new ArrayList<Object>(0)).getClass(), // RandomAccess
        Collections.unmodifiableList(new LinkedList<Object>()).getClass(), // Sequential

        // Set
        Collections.singleton(null).getClass(),
        Collections.unmodifiableSet(Collections.emptySet()).getClass(),
        UnionMethodSet.class,
        UnmodifiableArraySet.class,

        // SortedSet
        SingletonSortedSet.class,
        Collections.unmodifiableSortedSet(emptySortedSet()).getClass(),
    };

    /**
     * Gets the optimal implementation for unmodifiable collection.
     * If the collection is already unmodifiable, returns the same collection.
     * If collection is empty, uses <code>Collections.emptyList</code>.
     * If collection has one element, uses <code>Collections.singletonList</code>.
     * Otherwise, wraps the collection with <code>Collections.unmodifiableCollection</code>.
     */
    public static <T> Collection<T> optimalUnmodifiableCollection(Collection<T> collection) {
        int size = collection.size();
        if(size==0) return Collections.emptyList();
        Class<?> clazz = collection.getClass();
        for(int i=0, len=unmodifiableCollectionClasses.length; i<len; i++) if(unmodifiableCollectionClasses[i]==clazz) return collection;
        if(size==1) return Collections.singletonList(collection.iterator().next());
        return Collections.unmodifiableCollection(collection);
    }

    /**
     * Performs defensive shallow copy and returns unmodifiable collection.
     */
    public static <T> Collection<T> unmodifiableCopyCollection(Collection<T> collection) {
        int size = collection.size();
        if(size==0) return Collections.emptyList();
        // TODO: Create an unmodifiable collection that can only be populated here, and reused.
        // TODO: Goal is to protect from changes to original collection, while also not having
        // TODO: to copy repeatedly when different components use this same method for protection.
        // TODO: Also allow standard Collections singleton
        //Class<?> clazz = collection.getClass();
        //for(int i=0, len=unmodifiableCollectionClasses.length; i<len; i++) if(unmodifiableCollectionClasses[i]==clazz) return collection;
        if(size==1) return Collections.singletonList(collection.iterator().next());
        return Collections.unmodifiableCollection(new ArrayList<T>(collection));
    }

    private static final Class<?>[] unmodifiableListClasses = {
        Collections.singletonList(null).getClass(),
        Collections.unmodifiableList(new ArrayList<Object>(0)).getClass(), // RandomAccess
        Collections.unmodifiableList(new LinkedList<Object>()).getClass() // Sequential
    };

    /**
     * Gets the optimal implementation for unmodifiable list.
     * If list is empty, uses <code>Collections.emptyList</code>.
     * If list has one element, uses <code>Collections.singletonList</code>.
     * Otherwise, wraps the list with <code>Collections.unmodifiableList</code>.
     */
    public static <T> List<T> optimalUnmodifiableList(List<T> list) {
        int size = list.size();
        if(size==0) return Collections.emptyList();
        Class<?> clazz = list.getClass();
        for(int i=0, len=unmodifiableListClasses.length; i<len; i++) if(unmodifiableListClasses[i]==clazz) return list;
        if(size==1) return Collections.singletonList(list.get(0));
        return Collections.unmodifiableList(list);
    }

    /**
     * Performs defensive shallow copy and returns unmodifiable list.
     */
    public static <T> List<T> unmodifiableCopyList(Collection<T> collection) {
        int size = collection.size();
        if(size==0) return Collections.emptyList();
        // TODO: Create an unmodifiable collection that can only be populated here, and reused.
        // TODO: Goal is to protect from changes to original collection, while also not having
        // TODO: to copy repeatedly when different components use this same method for protection.
        // TODO: Also allow standard Collections singleton
        //Class<?> clazz = collection.getClass();
        //for(int i=0, len=unmodifiableListClasses.length; i<len; i++) if(unmodifiableListClasses[i]==clazz) return (List<T>)collection;
        if(size==1) return Collections.singletonList(collection.iterator().next());
        return Collections.unmodifiableList(new ArrayList<T>(collection));
    }

    private static final Class<?>[] unmodifiableSetClasses = {
        // Set
        Collections.singleton(null).getClass(),
        Collections.unmodifiableSet(Collections.emptySet()).getClass(),
        Collections.unmodifiableMap(Collections.emptyMap()).entrySet().getClass(),
        UnionMethodSet.class,
        UnmodifiableArraySet.class,

        // SortedSet
        SingletonSortedSet.class,
        Collections.unmodifiableSortedSet(emptySortedSet()).getClass()
    };

    /**
     * Gets the optimal implementation for unmodifiable set.
     * If set is empty, uses <code>Collections.emptySet</code>.
     * If set has one element, uses <code>Collections.singleton</code>.
     * Otherwise, wraps the set with <code>Collections.unmodifiableSet</code>.
     */
    public static <T> Set<T> optimalUnmodifiableSet(Set<T> set) {
        int size = set.size();
        if(size==0) return Collections.emptySet();
        Class<?> clazz = set.getClass();
        for(int i=0, len=unmodifiableSetClasses.length; i<len; i++) if(unmodifiableSetClasses[i]==clazz) return set;
        if(size==1) return Collections.singleton(set.iterator().next());
        return Collections.unmodifiableSet(set);
    }

    /**
     * Performs defensive shallow copy and returns unmodifiable set.
     * The iteration order of the original set is maintained.
     */
    public static <T> Set<T> unmodifiableCopySet(Collection<T> collection) {
        int size = collection.size();
        if(size==0) return Collections.emptySet();
        // TODO: Create an unmodifiable collection that can only be populated here, and reused.
        // TODO: Goal is to protect from changes to original collection, while also not having
        // TODO: to copy repeatedly when different components use this same method for protection.
        // TODO: Also allow standard Collections singleton
        //Class<?> clazz = collection.getClass();
        //for(int i=0, len=unmodifiableSetClasses.length; i<len; i++) if(unmodifiableSetClasses[i]==clazz) return (Set<T>)collection;
        if(size==1) return Collections.singleton(collection.iterator().next());
        return Collections.unmodifiableSet(new LinkedHashSet<T>(collection));
    }

    private static final Class<?>[] unmodifiableSortedSetClasses = {
        // SortedSet
        SingletonSortedSet.class,
        Collections.unmodifiableSortedSet(emptySortedSet()).getClass()
    };

    /**
     * Gets the optimal implementation for unmodifiable sorted set.
     * If sorted set is empty, uses <code>emptySortedSet</code>.
     * If sorted set has one element, uses <code>singletonSortedSet</code>.
     * Otherwise, wraps the sorted set with <code>Collections.unmodifiableSortedSet</code>.
     */
    public static <T> SortedSet<T> optimalUnmodifiableSortedSet(SortedSet<T> sortedSet) {
        int size = sortedSet.size();
        if(size==0) return emptySortedSet();
        Class<?> clazz = sortedSet.getClass();
        for(int i=0, len=unmodifiableSortedSetClasses.length; i<len; i++) if(unmodifiableSortedSetClasses[i]==clazz) return sortedSet;
        if(size==1) return singletonSortedSet(sortedSet.first());
        return Collections.unmodifiableSortedSet(sortedSet);
    }

    /**
     * Performs defensive shallow copy and returns unmodifiable sorted set.
     */
    public static <T> SortedSet<T> unmodifiableCopySortedSet(Collection<T> collection) {
        int size = collection.size();
        if(size==0) return emptySortedSet();
        // TODO: Create an unmodifiable collection that can only be populated here, and reused.
        // TODO: Goal is to protect from changes to original collection, while also not having
        // TODO: to copy repeatedly when different components use this same method for protection.
        // TODO: Also allow standard Collections singleton
        //Class<?> clazz = collection.getClass();
        //for(int i=0, len=unmodifiableSortedSetClasses.length; i<len; i++) if(unmodifiableSortedSetClasses[i]==clazz) return (SortedSet<T>)collection;
        if(size==1) return singletonSortedSet(collection.iterator().next());
        SortedSet<T> copy;
        if(collection instanceof SortedSet<?>) {
            copy = new TreeSet<T>((SortedSet<T>)collection);
        } else {
            copy = new TreeSet<T>(collection);
        }
        return Collections.unmodifiableSortedSet(copy);
    }

    private static final Class<?>[] unmodifiableMapClasses = {
        // Map
        Collections.emptyMap().getClass(),
        Collections.singletonMap(null, null).getClass(),
        Collections.unmodifiableMap(Collections.emptyMap()).getClass(),

        // SortedMap
        Collections.unmodifiableSortedMap(new TreeMap<Object,Object>()).getClass()
    };

    /**
     * Gets the optimal implementation for unmodifiable map.
     * If map is empty, uses <code>Collections.emptyMap</code>.
     * If map has one element, uses <code>Collections.singletonMap</code>.
     * Otherwise, wraps the map with <code>Collections.unmodifiableMap</code>.
     */
    public static <K,V> Map<K,V> optimalUnmodifiableMap(Map<K,V> map) {
        int size = map.size();
        if(size==0) return Collections.emptyMap();
        Class<?> clazz = map.getClass();
        for(int i=0, len=unmodifiableMapClasses.length; i<len; i++) if(unmodifiableMapClasses[i]==clazz) return map;
        if(size==1) {
            Map.Entry<? extends K,? extends V> entry = map.entrySet().iterator().next();
            return Collections.singletonMap(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Performs defensive shallow copy and returns unmodifiable map.
     * The iteration order of the original set is maintained.
     */
    public static <K,V> Map<K,V> unmodifiableCopyMap(Map<K,V> map) {
        int size = map.size();
        if(size==0) return Collections.emptyMap();
        // TODO: Create an unmodifiable collection that can only be populated here, and reused.
        // TODO: Goal is to protect from changes to original collection, while also not having
        // TODO: to copy repeatedly when different components use this same method for protection.
        // TODO: Also allow standard Collections singleton
        //Class<?> clazz = map.getClass();
        //for(int i=0, len=unmodifiableMapClasses.length; i<len; i++) if(unmodifiableMapClasses[i]==clazz) return map;
        if(size==1) {
            Map.Entry<? extends K,? extends V> entry = map.entrySet().iterator().next();
            return Collections.singletonMap(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(new LinkedHashMap<K,V>(map));
    }

    private static final Class<?>[] unmodifiableSortedMapClasses = {
        Collections.unmodifiableSortedMap(new TreeMap<Object,Object>()).getClass()
    };

    /**
     * Gets the optimal implementation for unmodifiable sorted map.
     * If sorted map is empty, uses <code>emptySortedMap</code>.
     * If sorted map has one element, uses <code>singletonSortedMap</code>.
     * Otherwise, wraps the sorted map with <code>Collections.unmodifiableSortedMap</code>.
     */
    public static <K,V> SortedMap<K,V> optimalUnmodifiableSortedMap(SortedMap<K,V> sortedMap) {
        // TODO: int size = sortedMap.size();
        // TODO: if(size==0) return emptySortedMap();
        Class<?> clazz = sortedMap.getClass();
        for(int i=0, len=unmodifiableSortedMapClasses.length; i<len; i++) if(unmodifiableSortedMapClasses[i]==clazz) return sortedMap;
        // TODO: if(size==1) {
        // TODO:     K key = sortedMap.firstKey();
        // TODO:     return singletonSortedMap(key, sortedMap.get(key));
        // TODO: }
        return Collections.unmodifiableSortedMap(sortedMap);
    }

    /**
     * Performs defensive shallow copy and returns unmodifiable sorted map.
     */
    public static <K,V> SortedMap<K,V> unmodifiableCopySortedMap(Map<K,V> map) {
        // TODO: int size = sortedMap.size();
        // TODO: if(size==0) return emptySortedMap();
        // TODO: Create an unmodifiable collection that can only be populated here, and reused.
        // TODO: Goal is to protect from changes to original collection, while also not having
        // TODO: to copy repeatedly when different components use this same method for protection.
        // TODO: Also allow standard Collections singleton
        //Class<?> clazz = map.getClass();
        //for(int i=0, len=unmodifiableSortedMapClasses.length; i<len; i++) if(unmodifiableSortedMapClasses[i]==clazz) return (SortedMap<K,V>)map;
        // TODO: if(size==1) {
        // TODO:     K key = sortedMap.firstKey();
        // TODO:     return singletonSortedMap(key, sortedMap.get(key));
        // TODO: }
        SortedMap<K,V> copy;
        if(map instanceof SortedMap<?,?>) {
            copy = new TreeMap<K,V>((SortedMap<K,V>)map);
        } else {
            copy = new TreeMap<K,V>(map);
        }
        return Collections.unmodifiableSortedMap(copy);
    }

    static class EmptyIterator<E> implements Iterator<E> {

        static final EmptyIterator instance = new EmptyIterator();

        private EmptyIterator() {
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public E next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Gets the empty iterator.
     */
    @SuppressWarnings("unchecked")
    public static <E> Iterator<E> emptyIterator() {
        return EmptyIterator.instance;
    }

    static class SingletonIterator<E> implements Iterator<E> {

        private final E value;
        private boolean hasNext = true;

        SingletonIterator(E value) {
            this.value = value;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public E next() {
            if(!hasNext) throw new NoSuchElementException();
            hasNext = false;
            return value;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Gets an unmodifiable iterator for a single object.
     */
    public static <E> Iterator<E> singletonIterator(E value) {
        return new SingletonIterator<E>(value);
    }

    static class UnmodifiableIterator<E> implements Iterator<E> {

        private final Iterator<E> iter;

        UnmodifiableIterator(Iterator<E> iter) {
            this.iter = iter;
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public E next() {
            return iter.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Wraps an iterator to make it unmodifiable.
     */
    public static <E> Iterator<E> unmodifiableIterator(Iterator<E> iter) {
        // Don't wrap already unmodifiable iterator types.
        if(
            (iter instanceof UnmodifiableIterator<?>)
            || (iter instanceof EnumerationIterator<?>)
            || (iter instanceof SingletonIterator<?>)
            || (iter==EmptyIterator.instance)
        ) return iter;
        return new UnmodifiableIterator<E>(iter);
    }

    /*
    private static void test() {
        List<Object> list = new ArrayList<Object>();
        list.add("One");
        list.add("Two");
        list = optimalUnmodifiableList(list);
        // Collection
        long startTime = System.currentTimeMillis();
        for(int c=0;c<100000000;c++) {
            optimalUnmodifiableList(list);
        }
        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("    Finished optimalUnmodifiableCollection in "+BigDecimal.valueOf(endTime, 3)+" sec");
    }

    public static void main(String[] args) {
        for(int c=0;c<30;c++) test();
    }*/

    /**
     * Allows peeking the first element of iteration.  Does not support remove.
     * Does not support null elements.
     */
    public static class PeekIterator<E> implements Iterator<E> {
        private final Iterator<? extends E> iter;
        private E next;
        PeekIterator(Iterator<? extends E> iter) {
            this.iter = iter;
            next = iter.hasNext() ? iter.next() : null;
        }

        @Override
        public boolean hasNext() {
            return next!=null;
        }

        @Override
        public E next() {
            E value = next;
            if(value==null) throw new NoSuchElementException();
            next = iter.hasNext() ? iter.next() : null;
            return value;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Gets the next value without removing it.
         *
         * @throws NoSuchElementException if no next value
         */
        public E peek() {
            E value = next;
            if(value==null) throw new NoSuchElementException();
            return value;
        }
    }

	/**
     * Wraps the provided iterator, allowing peek of first element.
     * Does not support null elements.
     */
    public static <E> PeekIterator<E> peekIterator(Iterator<? extends E> iter) {
        return new PeekIterator<E>(iter);
    }
	
	
}
