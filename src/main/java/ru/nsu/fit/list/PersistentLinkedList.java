package ru.nsu.fit.list;

import ru.nsu.fit.Interfaces.UndoRedoInterface;
import ru.nsu.fit.util.Constants;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import static java.util.Objects.isNull;

/**
 * Персистентный двусвязный список.
 */
public class PersistentLinkedList<E> implements List<E>, UndoRedoInterface {

    private final Stack<ListHead<ListItem<E>>> redo = new Stack<>();
    private final Stack<ListHead<ListItem<E>>> undo = new Stack<>();

    public PersistentLinkedList() {
        ListHead<ListItem<E>> head = new ListHead<>();
        undo.push(head);
        redo.clear();
    }

    public PersistentLinkedList(PersistentLinkedList<E> other) {
        this.undo.addAll(other.undo);
        this.redo.addAll(other.redo);
    }

    /**
     * Замена элемента по индексу.
     *
     * @param index индекс элемента.
     * @param element новый элемент.
     * @return замененный элемент.
     */
    @Override
    public E set(int index, E element) {
        return set(getCurrentHead(), index, element);
    }

    /**
     * Добавление в конец списка.
     *
     * @param element новый элемент.
     * @return true если добавлен успешно, иначе false.
     */
    @Override
    public boolean add(E element) {
        if (isFull()) {
            return false;
        }

        ListItem<E> listElement;
        ListHead<ListItem<E>> prevHead = getCurrentHead();
        ListHead<ListItem<E>> newHead;

        if (getCurrentHead().isEmpty()) {
            newHead = new ListHead<>();
            newHead.setFirst(0);
            newHead.setLast(0);
            listElement = new ListItem<>(element, -1, -1);
            newHead.add(listElement);
        } else {
            listElement = new ListItem<>(element, prevHead.getLast(), -1);

            newHead = new ListHead<>(prevHead);

            ListItem<E> elem = newHead.get(prevHead.getLast());

            ListItem<E> last = new ListItem<>(elem);

            last.setNext(newHead.getActualSize());

            newHead.set(prevHead.getLast(), last);

            newHead.setLast(newHead.getActualSize());
            newHead.add(listElement);
        }

        undo.push(newHead);
        redo.clear();

        return true;
    }

    /**
     * Добавление элемента по индексу.
     * Сдвигает элемент, находящийся в данный момент в этой позиции (если есть),
     * и любые последующие элементы вправо (+1 к индексам).
     *
     * @param index индекс вставки.
     * @param element новый элемент.
     */
    @Override
    public void add(int index, E element) {
        if (isFull()) {
            throw new IllegalStateException(Constants.FULL_LIST_MESSAGE);
        }

        ListHead<ListItem<E>> prevHead = getCurrentHead();
        ListHead<ListItem<E>> newHead = null;

        int indexBefore = -1;
        ListItem<E> beforeElement;

        int indexAfter = -1;
        ListItem<E> afterElement;

        int freeIndex = prevHead.getActualSize();

        if (prevHead.isEmpty()) {
            newHead = new ListHead<>(prevHead);
        } else {
            if (index != 0) {
                indexBefore = getTreeIndex(prevHead, index - 1);

                newHead = new ListHead<>(prevHead);
                beforeElement = new ListItem<>(newHead.get(indexBefore));

                beforeElement.setNext(freeIndex);
                newHead.set(indexBefore, beforeElement);
            }

            if (index != prevHead.getSize() - 1) {
                indexAfter = getTreeIndex(prevHead, index);

                ListHead<ListItem<E>> head = newHead;
                if (isNull(head)) {
                    head = prevHead;
                }

                newHead = new ListHead<>(head);

                afterElement = new ListItem<>(newHead.get(indexAfter));
                afterElement.setPrev(freeIndex);
                newHead.set(indexAfter, afterElement);
            }
        }

        undo.push(newHead);
        redo.clear();

        ListItem<E> listElement = new ListItem<>(element, indexBefore, indexAfter);

        if (indexBefore == -1 && !isNull(newHead)) {
            newHead.setFirst(freeIndex);
        }

        if (!isNull(newHead)) {
            newHead.add(listElement);
        }
    }

    /**
     * Удаление элемент по индексу.
     * Сдвигает любые последующие элементы влево (-1 к индексам).
     *
     * @param index индекс элемента.
     * @return удаленный элемент.
     */
    @Override
    public E remove(int index) {
        return remove(getCurrentHead(), index);
    }

    /**
     * Отчистка списка.
     */
    @Override
    public void clear() {
        ListHead<ListItem<E>> head = new ListHead<>();
        undo.push(head);
        redo.clear();
    }

    /**
     * Получение элемента по индексу.
     * @param index индекс элемента.
     *
     * @return найденный элемент.
     */
    @Override
    public E get(int index) {
        return get(getCurrentHead(), index);
    }

    /**
     * Преобразование элементов списка в строку.
     *
     * @return строка из элементов вида [a, b, c].
     */
    @Override
    public String toString() {
        ListHead<ListItem<E>> currentHead = getCurrentHead();

        if (currentHead.isEmpty()) {
            return currentHead.toString();
        }

        return Arrays.toString(toArray(currentHead));

    }

    /**
     * Преобразование списка в массив.
     *
     * @return массив элементов списка.
     */
    @Override
    public Object[] toArray() {
        return toArray(getCurrentHead());
    }

    /**
     * Итератор над персистентным списком.
     */
    public class PersistentListIterator<T> implements Iterator<T> {
        ListHead<ListItem<E>> head;
        ListItem<E> current;

        int i = 0;

        public PersistentListIterator() {
            this(getCurrentHead());
        }

        public PersistentListIterator(ListHead<ListItem<E>> head) {
            this.head = head;
            if (head.getSize() == 0) {
                return;
            }

            current = head.get(head.getFirst());
        }

        /**
         * Проверка наличия следующего элемента.
         *
         * @return true, если есть следующий.
         */
        @Override
        public boolean hasNext() {
            return head.getSize() > i;
        }

        /**
         * Получение следующего элемента списка.
         *
         * @return следующий элемент.
         */
        @Override
        public T next() {
            //noinspection unchecked
            T result = (T) current.getValue();
            i++;

            if (!hasNext()) {
                return result;
            }

            current = head.get(current.getNext());

            return result;
        }
    }

    /**
     * Отмена последнего изменения.
     */
    @Override
    public void undo() {
        if (!undo.empty()) {
            redo.push(undo.pop());
        }
    }

    /**
     * Отмена последнего undo().
     */
    @Override
    public void redo() {
        if (!redo.empty()) {
            undo.push(redo.pop());
        }
    }

    /**
     * Получение размера списка.
     *
     * @return размер списка.
     */
    @Override
    public int size() {
        return getHeadSize(getCurrentHead());
    }

    /**
     * Получение текущей головы списка.
     *
     * @return актуальная голова списка.
     */
    public ListHead<ListItem<E>> getCurrentHead() {
        return this.undo.peek();
    }

    /**
     * Проверка на полноту списка.
     *
     * @return true, если список полон.
     */
    public boolean isFull() {
        return isFull(getCurrentHead());
    }

    /**
     * Проверка на пустоту списка.
     *
     * @return true, если список пуст.
     */
    @Override
    public boolean isEmpty() {
        return getCurrentHead().isEmpty();
    }

    /**
     * Получение числа версий списка.
     *
     * @return число версий списка.
     */
    public int getVersionCount() {
        return undo.size() + redo.size();
    }

    private E set(ListHead<ListItem<E>> head, int index, E element) {
        E result = get(index);

        ListHead<ListItem<E>> newHead = new ListHead<>(head);
        ListItem<E> newNode = new ListItem<>(newHead.get(index));

        newNode.setValue(element);
        newHead.set(getTreeIndex(index), newNode);

        undo.push(newHead);
        redo.clear();

        return result;
    }

    private E remove(ListHead<ListItem<E>> prevHead, int index) {
        if (isFull()) {
            throw new IllegalStateException(Constants.FULL_LIST_MESSAGE);
        }

        ListHead<ListItem<E>> newHead;
        E result = get(index);

        if (prevHead.getSize() == 1) {
            undo.push(new ListHead<>());
            redo.clear();

            return result;
        }

        int treeIndex = getTreeIndex(prevHead, index);
        ListItem<E> mid = prevHead.get(treeIndex);

        int nextIndex = index + 1;
        int prevIndex = index - 1;
        newHead = new ListHead<>(prevHead);

        if (mid.getPrev() == -1) {
            int treeNextIndex = getTreeIndex(nextIndex);

            ListItem<E> next = getElement(newHead, nextIndex);
            ListItem<E> newNext = new ListItem<>(next);
            newNext.setPrev(-1);

            newHead.set(treeNextIndex, newNext);
            newHead.setFirst(treeNextIndex);

            finishRemove(newHead);

            return result;
        }

        if (mid.getNext() == -1) {
            int treePrevIndex = getTreeIndex(prevIndex);

            ListItem<E> prev = getElement(newHead, prevIndex);
            ListItem<E> newPrev = new ListItem<>(prev);

            newPrev.setNext(-1);

            newHead.set(treePrevIndex, newPrev);
            newHead.setLast(treePrevIndex);

            finishRemove(newHead);

            return result;
        }

        int treeNextIndex = getTreeIndex(nextIndex);
        int treePrevIndex = getTreeIndex(prevIndex);

        newHead = new ListHead<>(prevHead);

        ListItem<E> next = getElement(newHead, nextIndex);
        ListItem<E> newNext = new ListItem<>(next);

        newNext.setPrev(mid.getPrev());
        newHead.set(treeNextIndex, newNext);

        newHead = new ListHead<>(newHead);

        ListItem<E> prev = getElement(newHead, prevIndex);
        ListItem<E> newPrev = new ListItem<>(prev);

        newPrev.setNext(mid.getNext());

        newHead.set(treePrevIndex, newPrev);

        finishRemove(newHead);

        return result;
    }

    private void finishRemove(ListHead<ListItem<E>> newHead) {
        newHead.setSize(newHead.getSize() - 1);
        undo.push(newHead);
        redo.clear();
    }

    private int getTreeIndex(int listIndex) {
        return getTreeIndex(getCurrentHead(), listIndex);
    }

    private int getTreeIndex(ListHead<ListItem<E>> head, int index) {
        int trueIndex = head.getFirst();

        for (int i = 0; i < index; i++) {
            trueIndex = head.get(trueIndex).getNext();
        }

        return trueIndex;
    }

    private int getHeadSize(ListHead<ListItem<E>> head) {
        return head.getSize();
    }

    private E get(ListHead<ListItem<E>> head, int index) {
        if (index == 0) {
            return getValue(head, head.getFirst()).getValue();
        } else if (index == head.getSize() - 1) {
            return getValue(head, head.getLast()).getValue();
        } else {
            return getElement(head, index).getValue();
        }
    }

    private ListItem<E> getElement(ListHead<ListItem<E>> head, int index) {
        int treeIndex = getTreeIndex(index);

        return head.get(treeIndex);
    }

    private ListItem<E> getValue(ListHead<ListItem<E>> head, int index) {
        return head.get(index);
    }

    private Object[] toArray(ListHead<ListItem<E>> head) {
        Object[] objects = new Object[head.getSize()];
        Iterator<E> iterator = iterator(head);

        for (int i = 0; i < objects.length; i++) {
            objects[i] = iterator.next();
        }
        return objects;
    }

    private boolean isFull(ListHead<ListItem<E>> head) {
        return head.getActualSize() > head.getMaxSize();
    }

    /**
     * ignored overrides
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return a;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
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
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<E> listIterator() {
        return (ListIterator<E>) iterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return (ListIterator<E>) iterator();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return Collections.emptyList();
    }

    @Override
    public Iterator<E> iterator() {
        return new PersistentListIterator<>();
    }

    public Iterator<E> iterator(ListHead<ListItem<E>> head) {
        return new PersistentListIterator<>(head);
    }
}