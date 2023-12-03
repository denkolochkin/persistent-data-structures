package ru.nsu.fit.array;

import ru.nsu.fit.util.tree.BTree;

import java.util.*;

public class PersistentArray<T> implements List<T> {
    private static final int TABLE_SIZE = 8;

    BTree<T> bTree;

    public PersistentArray() {
        this(TABLE_SIZE);
    }

    public PersistentArray(int size) {
        bTree = new BTree<>(size);
    }

    @Override
    public int size() {
        return bTree.getSize();
    }


    @Override
    public boolean isEmpty() {
        return bTree.getSize() <= 0;
    }


    @Override
    public T set(int index, T element) {
        if(index < 0 || index >= size()){
            throw new IndexOutOfBoundsException();
        }

        T result = get(index);

        bTree = new BTree<T>(this.bTree);

        bTree.set(index, element);

        return result;
    }

    @Override
    public boolean add(T element) {
        BTree<T> tree = new BTree<>(bTree);

        return tree.add(element);
    }

    @Override
    public void add(int index, T element) {
        if(index < 0 || index >= size()){
            throw new IndexOutOfBoundsException();
        }

        BTree<T> oldHead = bTree;

        bTree = new BTree<>(bTree, index + 1, bTree.getMaxIndex(index));

        bTree.add(index, element);

        for (int i = index; i < oldHead.getSize(); i++) {
            bTree.add(oldHead.get(i));
        }
    }

    @Override
    public T remove(int index) {
        if(index < 0 || index >= size()){
            throw new IndexOutOfBoundsException();
        }

        T result = get(index);

        BTree<T> oldHead = bTree;

        bTree = new BTree<T>(bTree, index + 1, bTree.getMaxIndex(index));

        bTree.remove(index);

        for (int i = index + 1; i < oldHead.getSize(); i++) {
            bTree.add(oldHead.get(i));
        }

        return result;
    }

    @Override
    public void clear() {
        BTree<T> head = new BTree<>(bTree.getSize());
    }

    @Override
    public T get(int index) {
        return bTree.get(index);
    }

    @Override
    public String toString() {
        return Arrays.toString(this.toArray());
    }

    @Override
    public Object[] toArray() {
        Object[] objects = new Object[size()];

        for (int i = 0; i < objects.length; i++) {
            objects[i] = bTree.get(i);
        }

        return objects;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c)
            if (!contains(e))
                return false;

        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        BTree<T> tree = new BTree<>(bTree);
        boolean modified = false;
        for (T e : c) if (tree.add(e)) modified = true;
        return modified;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        BTree<T> oldHead = bTree;

        bTree = new BTree<>(bTree, index + 2, bTree.getMaxIndex(index));


        boolean modified = false;

        for (T e : c) {
            if (bTree.add(e)) modified = true;
        }

        for (int i = index; i < oldHead.getSize(); i++) {
            bTree.add(oldHead.get(i));
        }

        return modified;
    }

    @Override
    public boolean remove(Object o) {
        int indexOfElementToDelete = indexOf(o);

        if(indexOfElementToDelete == -1){
            return false;
        }

        remove(indexOfElementToDelete);

        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public int indexOf(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }

        BTree<T> bTree = this.bTree;
        for (int i = 0; i < bTree.getSize(); i++) {
            T element = bTree.get(i);
            if (o.equals(element)) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }

        BTree<T> bTree = this.bTree;
        for (int i = bTree.getSize() -1 ; i >= 0; i--) {
            T element = bTree.get(i);
            if (o.equals(element)) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public ListIterator<T> listIterator() {
        return new PersistentArrayIteratorList<>();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new PersistentArrayIteratorList<>(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        if(fromIndex < 0 || toIndex > bTree.getSize() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        return null;
    }

    @Override
    public Iterator<T> iterator() {
        return new PersistentArrayIterator<>();
    }

    public class PersistentArrayIterator<T> implements Iterator<T> {
        int index = 0;

        @Override
        public boolean hasNext() {
            return index < size();
        }

        @Override
        public T next() {
            return (T) PersistentArray.this.get(index++);
        }

        @Override
        public void remove() {
            PersistentArray.this.remove(index);
        }
    }

    public class PersistentArrayIteratorList<T> implements ListIterator<T> {
        int index;

        public PersistentArrayIteratorList(){
            index = 0;
        }

        public PersistentArrayIteratorList (int index){
            this.index = index;
        }

        @Override
        public boolean hasNext() {
            return index < size();
        }

        @Override
        public T next() {
            if(!hasNext()){
                throw new NoSuchElementException();
            }
            return (T) PersistentArray.this.get(index++);
        }

        @Override
        public boolean hasPrevious() {
            return index > 0;
        }

        @Override
        public T previous() {
            if(!hasPrevious()){
                throw new NoSuchElementException();
            }
            return (T) PersistentArray.this.get(--index);
        }

        @Override
        public int nextIndex() {
            return index;
        }

        @Override
        public int previousIndex() {
            return index - 1;
        }

        @Override
        public void remove() {
            PersistentArray.this.remove(index);
        }

        @Override
        public void set(T t) {
            //PersistentArray.this.set(index, (Class<E>) t);
        }

        @Override
        public void add(T t) {
            //PersistentArray.this.add(t);
        }
    }
}