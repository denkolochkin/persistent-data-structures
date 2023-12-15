package ru.nsu.fit.list;

import ru.nsu.fit.Interfaces.UndoRedoInterface;
import ru.nsu.fit.util.ListHead;
import ru.nsu.fit.util.ListItem;

import java.util.*;

import static java.util.Objects.isNull;

/**
 * Персистентный двусвязный список.
 */
public class PersistentLinkedList<E> implements List<E>, UndoRedoInterface {
    private final Stack<ListHead<ListItem<E>>> redoStack = new Stack<>();
    private final Stack<ListHead<ListItem<E>>> undoStack = new Stack<>();

    public PersistentLinkedList() {
        ListHead<ListItem<E>> head = new ListHead<>();
        updateUndoRedoStack(head);
    }

    public PersistentLinkedList(PersistentLinkedList<E> other) {
        this.undoStack.addAll(other.undoStack);
        this.redoStack.addAll(other.redoStack);
    }

    /**
     * Замена элемента по индексу.
     *
     * @param index   индекс элемента.
     * @param element новый элемент.
     * @return замененный элемент.
     */
    @Override
    public E set(int index, E element) {
        return set(takeLatestVersion(), index, element);
    }

    /**
     * Добавление в конец списка.
     *
     * @param element новый элемент.
     * @return true если добавлен успешно, иначе false.
     */
    @Override
    public boolean add(E element) {
        ListItem<E> listElement;
        ListHead<ListItem<E>> prevHead = takeLatestVersion();
        ListHead<ListItem<E>> newHead;

        if (prevHead.isEmpty()) {
            newHead = new ListHead<>();
            newHead.setFirstIndex(0);
            newHead.setLastIndex(0);
            listElement = new ListItem<>(element, -1, -1);
            newHead.add(listElement);
        } else {
            listElement = new ListItem<>(element, prevHead.getLastIndex(), -1);

            newHead = new ListHead<>(prevHead);

            ListItem<E> item = newHead.get(prevHead.getLastIndex());

            ListItem<E> last = new ListItem<>(item);

            last.setNextIndex(newHead.getActualSize());

            newHead.set(prevHead.getLastIndex(), last);

            newHead.setLastIndex(newHead.getActualSize());
            newHead.add(listElement);
        }

        updateUndoRedoStack(newHead);

        return true;
    }

    /**
     * Добавление элемента по индексу.
     * Сдвигает элемент, находящийся в данный момент в этой позиции (если есть),
     * и любые последующие элементы вправо (+1 к индексам).
     *
     * @param index   индекс вставки.
     * @param element новый элемент.
     */
    @Override
    public void add(int index, E element) {
        ListHead<ListItem<E>> prevHead = takeLatestVersion();
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

                beforeElement.setNextIndex(freeIndex);
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
                afterElement.setPrevIndex(freeIndex);
                newHead.set(indexAfter, afterElement);
            }
        }

        ListItem<E> listElement = new ListItem<>(element, indexBefore, indexAfter);

        if (indexBefore == -1 && !isNull(newHead)) {
            newHead.setFirstIndex(freeIndex);
        }

        if (!isNull(newHead)) {
            newHead.add(listElement);
        }

        updateUndoRedoStack(newHead);
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
        return remove(takeLatestVersion(), index);
    }

    /**
     * Отчистка списка.
     */
    @Override
    public void clear() {
        ListHead<ListItem<E>> head = new ListHead<>();
        updateUndoRedoStack(head);
    }

    /**
     * Получение элемента по индексу.
     *
     * @param index индекс элемента.
     * @return найденный элемент.
     */
    @Override
    public E get(int index) {
        return get(takeLatestVersion(), index);
    }

    /**
     * Преобразование элементов списка в строку.
     *
     * @return строка из элементов вида [a, b, c].
     */
    @Override
    public String toString() {
        ListHead<ListItem<E>> currentHead = takeLatestVersion();

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
        return toArray(takeLatestVersion());
    }

    /**
     * Итератор над персистентным списком.
     */
    public class PersistentListIterator<T> implements Iterator<T> {
        ListHead<ListItem<E>> head;
        ListItem<E> current;

        int i = 0;

        public PersistentListIterator() {
            this(takeLatestVersion());
        }

        public PersistentListIterator(ListHead<ListItem<E>> head) {
            this.head = head;
            if (head.getSize() == 0) {
                return;
            }

            current = head.get(head.getFirstIndex());
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

            current = head.get(current.getNextIndex());

            return result;
        }
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
     * Получение размера списка.
     *
     * @return размер списка.
     */
    @Override
    public int size() {
        return getHeadSize(takeLatestVersion());
    }

    /**
     * Получение текущей головы списка.
     *
     * @return актуальная голова списка.
     */
    public ListHead<ListItem<E>> takeLatestVersion() {
        return this.undoStack.peek();
    }

    /**
     * Проверка на пустоту списка.
     *
     * @return true, если список пуст.
     */
    @Override
    public boolean isEmpty() {
        return takeLatestVersion().isEmpty();
    }

    /**
     * Получение числа версий списка.
     *
     * @return число версий списка.
     */
    public int getVersionCount() {
        return undoStack.size() + redoStack.size();
    }

    private E set(ListHead<ListItem<E>> head, int index, E element) {
        E result = get(index);

        ListHead<ListItem<E>> newHead = new ListHead<>(head);
        ListItem<E> newNode = new ListItem<>(newHead.get(index));

        newNode.setValue(element);
        newHead.set(getTreeIndex(index), newNode);

        updateUndoRedoStack(newHead);

        return result;
    }

    private E remove(ListHead<ListItem<E>> prevHead, int index) {
        ListHead<ListItem<E>> newHead;
        E result = get(index);

        if (prevHead.getSize() == 1) {
            updateUndoRedoStack(new ListHead<>());
            return result;
        }

        int treeIndex = getTreeIndex(prevHead, index);
        ListItem<E> mid = prevHead.get(treeIndex);

        int nextIndex = index + 1;
        int prevIndex = index - 1;
        newHead = new ListHead<>(prevHead);

        if (mid.getPrevIndex() == -1) {
            int treeNextIndex = getTreeIndex(nextIndex);

            ListItem<E> next = getElement(newHead, nextIndex);
            ListItem<E> newNext = new ListItem<>(next);
            newNext.setPrevIndex(-1);

            newHead.set(treeNextIndex, newNext);
            newHead.setFirstIndex(treeNextIndex);
        }else

        if (mid.getNextIndex() == -1) {
            int treePrevIndex = getTreeIndex(prevIndex);

            ListItem<E> prev = getElement(newHead, prevIndex);
            ListItem<E> newPrev = new ListItem<>(prev);

            newPrev.setNextIndex(-1);

            newHead.set(treePrevIndex, newPrev);
            newHead.setLastIndex(treePrevIndex);
        }else {

            int treeNextIndex = getTreeIndex(nextIndex);
            int treePrevIndex = getTreeIndex(prevIndex);

            newHead = new ListHead<>(prevHead);

            ListItem<E> next = getElement(newHead, nextIndex);
            ListItem<E> newNext = new ListItem<>(next);

            newNext.setPrevIndex(mid.getPrevIndex());
            newHead.set(treeNextIndex, newNext);

            newHead = new ListHead<>(newHead);

            ListItem<E> prev = getElement(newHead, prevIndex);
            ListItem<E> newPrev = new ListItem<>(prev);

            newPrev.setNextIndex(mid.getNextIndex());

            newHead.set(treePrevIndex, newPrev);
        }

        newHead.setSize(newHead.getSize() - 1);

        updateUndoRedoStack(newHead);

        return result;
    }

    private int getTreeIndex(int listIndex) {
        return getTreeIndex(takeLatestVersion(), listIndex);
    }

    private int getTreeIndex(ListHead<ListItem<E>> head, int index) {
        int trueIndex = head.getFirstIndex();

        for (int i = 0; i < index; i++) {
            trueIndex = head.get(trueIndex).getNextIndex();
        }

        return trueIndex;
    }

    private int getHeadSize(ListHead<ListItem<E>> head) {
        return head.getSize();
    }

    private E get(ListHead<ListItem<E>> head, int index) {
        return getElement(head, index).getValue();
    }

    private ListItem<E> getElement(ListHead<ListItem<E>> head, int index) {
        int treeIndex = getTreeIndex(index);

        return head.get(treeIndex);
    }

    private Object[] toArray(ListHead<ListItem<E>> head) {
        Object[] objects = new Object[head.getSize()];
        Iterator<E> iterator = iterator(head);

        for (int i = 0; i < objects.length; i++) {
            objects[i] = iterator.next();
        }
        return objects;
    }

    private void updateUndoRedoStack(ListHead<ListItem<E>> head){
        undoStack.push(head);
        redoStack.clear();
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