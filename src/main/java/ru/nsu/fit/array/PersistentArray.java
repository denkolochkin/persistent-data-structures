package ru.nsu.fit.array;


import ru.nsu.fit.Interfaces.UndoRedoInterface;
import ru.nsu.fit.util.BTree;

import java.util.*;

/**
 * PersistentArray использует копирование пути для Б-дерева.
 */
public class PersistentArray<T> implements List<T>, UndoRedoInterface {
    private static final int ARRAY_SIZE = 8;
    protected final Stack<BTree<T>> redoStack = new Stack<>();
    protected final Stack<BTree<T>> undoStack = new Stack<>();

    public PersistentArray() {
        this(ARRAY_SIZE);
    }

    public PersistentArray(int size) {
        BTree<T> bTree = new BTree<>(size);
        updateRedoUndoStack(bTree);
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

        BTree<T> bTree = new BTree<>(takeLatestVersion());

        bTree.set(index, element);

        updateRedoUndoStack(bTree);

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
        BTree<T> bTree = new BTree<>(takeLatestVersion());
        updateRedoUndoStack(bTree);
        return bTree.add(element);
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

        BTree<T> tbTree = takeLatestVersion();

        BTree<T> bTree = new BTree<>(takeLatestVersion(), index + 1);

        bTree.set(index, element);

        for (int i = index; i < tbTree.getSize(); i++) {
            bTree.add(tbTree.get(i));
        }

        updateRedoUndoStack(bTree);
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

        BTree<T> tbTree = takeLatestVersion();

        BTree<T> bTree = new BTree<>(takeLatestVersion(), index + 1);

        bTree.remove(index);

        for (int i = index + 1; i < tbTree.getSize(); i++) {
            bTree.add(tbTree.get(i));
        }

        updateRedoUndoStack(bTree);

        return result;
    }

    /**
     * Удаляет все элементы из этого массива.
     * Массив будет пуст после возврата этого вызова.
     */
    @Override
    public void clear() {
        BTree<T> bTree = new BTree<>(takeLatestVersion().getSize());
        updateRedoUndoStack(bTree);
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
    public <E> E[] toArray(E[] a) {
        return a;
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
        BTree<T> bTree = new BTree<>(takeLatestVersion());

        boolean modified = false;
        for (T e : c) if (bTree.add(e)) modified = true;

        updateRedoUndoStack(bTree);

        return modified;
    }

    /**
     * Добавление коллекции элементов начиная с индекса.
     *
     * @param index индекс, начиная с которого происходит добавление.
     * @param c     элементы для добавления.
     * @return true, если добавлены успешно.
     */
    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        BTree<T> tbTree = takeLatestVersion();

        BTree<T> bTree = new BTree<>(takeLatestVersion(), index);

        boolean modified = false;

        for (T e : c) {
            if (bTree.add(e)) modified = true;
        }

        for (int i = index; i < tbTree.getSize(); i++) {
            bTree.add(tbTree.get(i));
        }

        updateRedoUndoStack(bTree);

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

    /**
     * ignored override
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    /**
     * ignored override
     */
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

    /**
     * ignored override
     */
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
    public class PersistentArrayIterator<E extends T> implements Iterator<T> {
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
        public E next() {
            return (E) PersistentArray.this.get(index++);
        }

        @Override
        public void remove() {
            PersistentArray.this.remove(index);
        }
    }


    /**
     * Итератор списка над персистентным массивом.
     */
    public class PersistentArrayIteratorList<E extends T> implements ListIterator<T> {
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
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return (E) PersistentArray.this.get(index++);
        }

        @Override
        public boolean hasPrevious() {
            return index > 0;
        }

        @Override
        public E previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            return (E) PersistentArray.this.get(--index);
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
        public void set(T e) {
            PersistentArray.this.set(index, e);
        }

        @Override
        public void add(T e) {
            PersistentArray.this.add(e);
        }
    }

    private BTree<T> takeLatestVersion() {
        return this.undoStack.peek();
    }

    private void updateRedoUndoStack(BTree<T> bTree){
        undoStack.push(bTree);
        redoStack.clear();
    }
}