package ru.nsu.fit.array;


import ru.nsu.fit.Interfaces.UndoRedoInterface;
import ru.nsu.fit.util.BTree;

import java.util.*;

/**
 * PersistentArray использует копирование пути для Б-дерева.
 */
public class PersistentArray<T> implements List<T>, UndoRedoInterface {
    private static final int TABLE_SIZE = 8;
    protected final Stack<BTree<T>> redoStack = new Stack<>();
    protected final Stack<BTree<T>> undoStack = new Stack<>();

    public PersistentArray() {
        this(TABLE_SIZE);
    }

    public PersistentArray(int size) {
        BTree<T> head = new BTree<>(size);
        undoStack.push(head);
        redoStack.clear();
    }

    public PersistentArray(PersistentArray<T> other) {
        this.undoStack.addAll(other.undoStack);
        this.redoStack.addAll(other.redoStack);
    }

    /**
     * Отмена последнего изменения.
     */
    @Override
    public void undo() {
        if (!undoStack.empty()) {
            redoStack.push(undoStack.pop());
        }
    }

    /**
     * Отмена последнего undo().
     */
    @Override
    public void redo() {
        if (!redoStack.empty()) {
            undoStack.push(redoStack.pop());
        }
    }

    /**
     * Возвращает количество элементов в массиве.
     *
     * @return количество элементов в массиве
     */
    @Override
    public int size() {
        return takeLatestVersion().getSize();
    }

    /**
     * Возвращает true, если массив пуст.
     *
     * @return true, если массив пуст
     */
    @Override
    public boolean isEmpty() {
        return takeLatestVersion().getSize() <= 0;
    }

    /**
     * Заменяет элемент массива.
     *
     * @param index   индекс элемента который надо заменить
     * @param element новый элемент
     * @return заменённый элемент
     */
    @Override
    public T set(int index, T element) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }

        T result = get(index);

        BTree<T> bTree = new BTree<T>(takeLatestVersion());
        undoStack.push(bTree);
        redoStack.clear();

        bTree.set(index, element);

        return result;
    }

    /**
     * Добавление элемента в конец массива.
     *
     * @param element элемент.
     * @return true если массив изменился в результате вызова.
     */
    @Override
    public boolean add(T element) {
        BTree<T> tree = new BTree<>(takeLatestVersion());
        undoStack.push(tree);
        redoStack.clear();
        return tree.add(element);
    }

    public PersistentArray<T> conj(T element) {
        PersistentArray<T> result = new PersistentArray<>(this);
        result.add(element);
        return result;
    }

    public PersistentArray<T> assoc(int index, T element) {
        PersistentArray<T> result = new PersistentArray<>(this);
        result.set(index, element);
        return result;
    }

    /**
     * Вставляет указанный элемент в указанную позицию в этом списке
     * <p>
     * Сдвигает элемент, находящийся в данный момент в этой позиции (если есть),
     * и любые последующие элементы вправо (добавляет единицу к их индексам).
     * </p>
     *
     * @param index   индекс, указание позиции
     * @param element элемент
     */
    @Override
    public void add(int index, T element) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }

        BTree<T> oldHead = takeLatestVersion();

        BTree<T> bTree = new BTree<>(takeLatestVersion(), index + 1);
        undoStack.push(bTree);
        redoStack.clear();

        bTree.set(index, element);

        for (int i = index; i < oldHead.getSize(); i++) {
            bTree.add(oldHead.get(i));
        }
    }

    /**
     * Удаляет элемент в указанной позиции.
     * <p>
     * Сдвигает любые последующие элементы влево (вычитает единицу из их индексов).
     * Возвращает элемент, который был удален из массива.
     * </p>
     *
     * @param index позиция элемента
     * @return удаленный элемент
     */
    @Override
    public T remove(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }

        T result = get(index);

        BTree<T> oldHead = takeLatestVersion();

        BTree<T> newHead = new BTree<>(takeLatestVersion(), index + 1);
        undoStack.push(newHead);
        redoStack.clear();

        newHead.remove(index);

        for (int i = index + 1; i < oldHead.getSize(); i++) {
            newHead.add(oldHead.get(i));
        }

        return result;
    }

    /**
     * Удаляет все элементы из этого массива.
     * Массив будет пуст после возврата этого вызова.
     */
    @Override
    public void clear() {
        BTree<T> head = new BTree<>(takeLatestVersion().getSize());
        undoStack.push(head);
        redoStack.clear();
    }

    /**
     * Получение элемента по индексу.
     *
     * @param index индекс элемента.
     * @return Найденный элемент, либо IndexOutOfBoundsException при некорректном индексе.
     */
    @Override
    public T get(int index) {
        return takeLatestVersion().get(index);
    }

    /**
     * Преобразование элементов массива в строку.
     *
     * @return строка из элементов вида [a, b, c].
     */
    @Override
    public String toString() {
        return Arrays.toString(this.toArray());
    }

    /**
     * Преобразование элементов в массив.
     *
     * @return массив элементов.
     */
    @Override
    public Object[] toArray() {
        Object[] objects = new Object[size()];

        for (int i = 0; i < objects.length; i++) {
            objects[i] = takeLatestVersion().get(i);
        }

        return objects;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    /**
     * Проверка элемента на вхождение в массиве.
     *
     * @param o элемент для проверки.
     * @return true, если элемент содержится в массиве.
     */
    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    /**
     * Проверка элементов на вхождение в массиве.
     *
     * @param c коллекция элементов для проверки.
     * @return true, если все элементы коллекции содержатся в массиве.
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c)
            if (!contains(e))
                return false;

        return true;
    }

    /**
     * Добавление коллекции элементов.
     *
     * @param c элементы для добавления.
     * @return true, если добавлены успешно.
     */
    @Override
    public boolean addAll(Collection<? extends T> c) {
        BTree<T> tree = new BTree<>(takeLatestVersion());
        undoStack.push(tree);
        redoStack.clear();

        boolean modified = false;
        for (T e : c) if (tree.add(e)) modified = true;
        return modified;
    }

    /**
     * Добавление коллекции элементов начиная с индекса.
     *
     * @param index индекс, начиная с которого происходит добавление.
     * @param c элементы для добавления.
     * @return true, если добавлены успешно.
     */
    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        BTree<T> oldHead = takeLatestVersion();

        BTree<T> newHead = new BTree<>(takeLatestVersion(), index);
        undoStack.push(newHead);
        redoStack.clear();


        boolean modified = false;

        for (T e : c) {
            if (newHead.add(e)) modified = true;
        }

        for (int i = index; i < oldHead.getSize(); i++) {
            newHead.add(oldHead.get(i));
        }

        return modified;
    }

    /**
     * Удаление элемента из массива.
     *
     * @param o удаляемый элемент.
     * @return true, если удален успешно.
     */
    @Override
    public boolean remove(Object o) {
        int indexOfElementToDelete = indexOf(o);

        if (indexOfElementToDelete == -1) {
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

    /**
     * Определение индекса элемента в массиве.
     *
     * @param o элемент.
     * @return индекс элемента.
     */
    @Override
    public int indexOf(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }

        BTree<T> bTree = takeLatestVersion();
        for (int i = 0; i < bTree.getSize(); i++) {
            T element = bTree.get(i);
            if (o.equals(element)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Определение последнего индекса элемента в массиве.
     *
     * @param o элемент.
     * @return последний индекс элемента.
     */
    @Override
    public int lastIndexOf(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }

        BTree<T> bTree = takeLatestVersion();
        for (int i = bTree.getSize() - 1; i >= 0; i--) {
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
        if (fromIndex < 0 || toIndex > takeLatestVersion().getSize() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        return null;
    }

    @Override
    public Iterator<T> iterator() {
        return new PersistentArrayIterator<>();
    }

    /**
     * Итератор над персистентным массивом.
     */
    public class PersistentArrayIterator<T> implements Iterator<T> {
        int index = 0;

        /**
         * Проверка наличия следующего элемента.
         *
         * @return true, если есть следующий.
         */
        @Override
        public boolean hasNext() {
            return index < size();
        }

        /**
         * Получение следующего элемента массива.
         *
         * @return следующий элемент.
         */
        @Override
        public T next() {
            return (T) PersistentArray.this.get(index++);
        }

        @Override
        public void remove() {
            PersistentArray.this.remove(index);
        }
    }


    /**
     * Итератор списка над персистентным массивом.
     */
    public class PersistentArrayIteratorList<T> implements ListIterator<T> {
        int index;

        public PersistentArrayIteratorList() {
            index = 0;
        }

        public PersistentArrayIteratorList(int index) {
            this.index = index;
        }

        /**
         * Проверка наличия следующего элемента.
         *
         * @return true, если есть следующий.
         */
        @Override
        public boolean hasNext() {
            return index < size();
        }

        /**
         * Получение следующего элемента массива.
         *
         * @return следующий элемент.
         */
        @Override
        public T next() {
            if (!hasNext()) {
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
            if (!hasPrevious()) {
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

    private BTree<T> takeLatestVersion() {
        return this.undoStack.peek();
    }
}