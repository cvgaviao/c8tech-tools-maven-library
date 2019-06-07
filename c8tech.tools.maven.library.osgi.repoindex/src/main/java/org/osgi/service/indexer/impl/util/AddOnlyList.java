/**
 * ======================================================================
 * Copyright © 2015-2019, OSGi Alliance, Cristiano V. Gavião.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * =======================================================================
 */
package org.osgi.service.indexer.impl.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

class AddOnlyIterator<T> implements Iterator<T> {

    private final Iterator<T> iter;

    AddOnlyIterator(Iterator<T> iter) {
        this.iter = iter;
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public T next() {
        return iter.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException(AddOnlyList.ERROR_REMOVE);
    }
}

/**
 * <p>
 * A List implementation in which destructive removal or replacement of elements
 * is forbidden. Any attempt to either <b>remove</b> elements (e.g. through
 * {@link #remove(int)}, {@link #clear()}, {@link Iterator#remove()}) or
 * <b>replace</b> elements (e.g. through {@link #set(int, Object)} or
 * {@link ListIterator#set(Object)}) will throw an
 * {@link UnsupportedOperationException}.
 * </p>
 * 
 * <p>
 * Note that this is a wrapper class only. It must be initialized with an actual
 * {@link List} implementation for its underlying data structure.
 * </p>
 * 
 * @author Neil Bartlett
 * 
 * @param <T>
 *            The type of the list.
 */
public class AddOnlyList<T> extends LinkedList<T> {

    /**
     * 
     */
    private static final long serialVersionUID = -8909342551974718439L;
    static final String ERROR_REMOVE = "Removal of items is not permitted.";
    static final String ERROR_REPLACE = "Replacement of items is not permitted.";

    public AddOnlyList() {
        super();
    }

    /**
     * Create a new add-only list based on the specified underlying list.
     * 
     * @param list
     *                 The list providing the underlying data structure.
     */
    public AddOnlyList(Collection<? extends T> list) {
        super(list);
    }

    // FORBIDDEN METHODS: remove, removeAll, retailAll, clear, set

    @Override
    public void add(int index, T element) {
        if (!this.contains(element)) {
            super.add(index, element);
        }
    }

    @Override
    public boolean add(T e) {
        if (super.contains(e)) {
            return false;
        } else {
            return super.add(e);
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        Collection<T> copy = new LinkedList<>(c);
        copy.removeAll(this);
        return super.addAll(copy);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        Collection<T> copy = new LinkedList<>(c);
        copy.removeAll(this);
        return super.addAll(index, copy);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(ERROR_REMOVE);
    }

    // WRAPPING METHODS: create restricted iterators and sublists

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    // STRAIGHT-THROUGH DELEGATED METHODS

    @Override
    public Iterator<T> iterator() {
        return new AddOnlyIterator<>(super.iterator());
    }

    @Override
    public ListIterator<T> listIterator() {
        return new AddOnlyListIterator<>(super.listIterator());
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new AddOnlyListIterator<>(super.listIterator(index));
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException(ERROR_REMOVE);
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(ERROR_REMOVE);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException(ERROR_REMOVE);
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        throw new UnsupportedOperationException(ERROR_REMOVE);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        throw new UnsupportedOperationException(ERROR_REMOVE);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException(ERROR_REMOVE);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException(ERROR_REMOVE);
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException(ERROR_REPLACE);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return new AddOnlyList<>(super.subList(fromIndex, toIndex));
    }

}

class AddOnlyListIterator<T> implements ListIterator<T> {

    private final ListIterator<T> iter;

    AddOnlyListIterator(ListIterator<T> iter) {
        this.iter = iter;
    }

    @Override
    public void add(T e) {
        iter.add(e);
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public boolean hasPrevious() {
        return iter.hasPrevious();
    }

    @Override
    public T next() {
        return iter.next();
    }

    @Override
    public int nextIndex() {
        return iter.nextIndex();
    }

    @Override
    public T previous() {
        return iter.previous();
    }

    @Override
    public int previousIndex() {
        return iter.previousIndex();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException(AddOnlyList.ERROR_REMOVE);
    }

    @Override
    public void set(T e) {
        throw new UnsupportedOperationException(AddOnlyList.ERROR_REPLACE);
    }
}
