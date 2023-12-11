package ru.nsu.fit.list;

import javafx.util.Pair;
import ru.nsu.fit.Interfaces.UndoRedoInterface;
import ru.nsu.fit.util.Constants;
import ru.nsu.fit.util.node.Node;
import ru.nsu.fit.util.tree.BTree;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

/**
 * Персистентный двусвязный список.
 */
public class PersistentLinkedList<E> implements List<E>, UndoRedoInterface {

    private final BTree<E> bTree;

    private final Stack<ListHead<ListItem<E>>> redo = new Stack<>();
    private final Stack<ListHead<ListItem<E>>> undo = new Stack<>();

    private final Stack<PersistentLinkedList<?>> undoValues = new Stack<>();
    private final Stack<PersistentLinkedList<?>> redoValues = new Stack<>();

    private PersistentLinkedList<PersistentLinkedList<?>> parent;

    public PersistentLinkedList() {
        this.bTree = new BTree<>(2, 6);
        ListHead<ListItem<E>> head = new ListHead<>();
        undo.push(head);
        redo.clear();
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
        Pair<Integer, Boolean> next;

        if (getCurrentHead().getSize() == 0) {
            newHead = new ListHead<>();
            newHead.setFirst(0);
            newHead.setLast(0);
            listElement = new ListItem<>(element, -1, -1);
            bTree.findLeaf(newHead).getValue().add(listElement);
        } else {
            listElement = new ListItem<>(element, prevHead.getLast(), -1);

            newHead = new ListHead<>(prevHead, 0);
            Pair<Node<ListItem<E>>, Integer> node = bTree.copyLeaf(newHead, prevHead.getLast());
            int leafIndex = node.getValue();
            Node<ListItem<E>> copy = node.getKey();

            ListItem<E> last = new ListItem<>(copy.getValue().get(leafIndex));
            copy.getValue().set(leafIndex, last);

            next = getNextIndex(newHead);
            if (Boolean.FALSE.equals(next.getValue())) {
                last.setNext(newHead.getSizeTree());
                newHead.setLast(newHead.getSizeTree());
            } else {
                last.setNext(next.getKey());
                ListItem<E> old = new ListItem<>(getValue(newHead, next.getKey()));
                Pair<Node<ListItem<E>>, Integer> oldLeaf = bTree.getLeaf(newHead, next.getKey());
                oldLeaf.getKey().getValue().set(oldLeaf.getValue(), old);

                old.setValue(element);
                old.setNext(-1);
                old.setPrev(prevHead.getLast());

                newHead.setLast(last.getNext());
                newHead.setSize(newHead.getSize() + 1);
            }

            if (Boolean.FALSE.equals(next.getValue())) {
                bTree.findLeaf(newHead).getValue().add(listElement);
            }
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
        checkListIndex(index);

        ListHead<ListItem<E>> prevHead = getCurrentHead();
        ListHead<ListItem<E>> newHead = null;

        int indexBefore = -1;
        ListItem<E> beforeElement;

        int indexAfter = -1;
        ListItem<E> afterElement;

        int freeIndex = prevHead.getSizeTree();

        if (prevHead.getSize() == 0) {
            newHead = new ListHead<>(prevHead);
        } else {
            if (index != 0) {
                indexBefore = getTreeIndex(prevHead, index - 1);

                newHead = new ListHead<>(prevHead, 0);
                Pair<Node<ListItem<E>>, Integer> node = bTree.copyLeaf(newHead, indexBefore);
                Node<ListItem<E>> copy = node.getKey();
                int leafIndex = node.getValue();

                beforeElement = new ListItem<>(copy.getValue().get(leafIndex));
                beforeElement.setNext(freeIndex);
                copy.getValue().set(leafIndex, beforeElement);
            }

            if (index != prevHead.getSize() - 1) {
                indexAfter = getTreeIndex(prevHead, index);
                ListHead<ListItem<E>> head = newHead != null ? newHead : prevHead;
                newHead = new ListHead<>(head, 0);
                Pair<Node<ListItem<E>>, Integer> node = bTree.copyLeaf(newHead, indexAfter);
                int leafIndex = node.getValue();
                Node<ListItem<E>> copy = node.getKey();

                afterElement = new ListItem<>(copy.getValue().get(leafIndex));
                afterElement.setPrev(freeIndex);
                copy.getValue().set(leafIndex, afterElement);
            }
        }

        undo.push(newHead);
        redo.clear();
        parentUndo(element);

        ListItem<E> listElement = new ListItem<>(element, indexBefore, indexAfter);

        if (indexBefore == -1 && newHead != null) {
            newHead.setFirst(freeIndex);
        }

        if (indexAfter == -1 && newHead != null) {
            newHead.setLast(freeIndex);
        }

        assert newHead != null;
        bTree.findLeaf(newHead).getValue().add(listElement);
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

    @Override
    public String toString() {
        ListHead<ListItem<E>> currentHead = getCurrentHead();

        if (currentHead.getSize() == 0) {
            return currentHead.toString();
        }

        return Arrays.toString(toArray(currentHead));

    }

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

            Pair<Node<ListItem<E>>, Integer> leaf = bTree.getLeaf(head, head.getFirst());
            current = leaf.getKey().getValue().get(leaf.getValue());
        }

        @Override
        public boolean hasNext() {
            return head.getSize() > i;
        }

        @Override
        public T next() {
            //noinspection unchecked
            T result = (T) current.getValue();
            i++;

            if (!hasNext()) {
                return result;
            }

            Pair<Node<ListItem<E>>, Integer> tmp = bTree.getLeaf(head, current.getNext());
            current = tmp.getKey().getValue().get(tmp.getValue());
            return result;
        }
    }

    @Override
    public void undo() {
        if (!undoValues.empty()) {
            undoValues.peek().undo();
            redoValues.push(undoValues.pop());
        } else {
            if (!undo.empty()) {
                redo.push(undo.pop());
            }
        }
    }

    @Override
    public void redo() {
        if (!redoValues.empty()) {
            redoValues.peek().redo();
            undoValues.push(redoValues.pop());
        } else {
            if (!redo.empty()) {
                undo.push(redo.pop());
            }
        }
    }


    @Override
    public int size() {
        return size(getCurrentHead());
    }

    public int size(ListHead<ListItem<E>> head) {
        return head.getSize();
    }

    public ListHead<ListItem<E>> getCurrentHead() {
        return this.undo.peek();
    }

    public boolean isFull() {
        return isFull(getCurrentHead());
    }

    @Override
    public boolean isEmpty() {
        return getCurrentHead().getSize() <= 0;
    }

    public int getVersionCount() {
        return undo.size() + redo.size();
    }

    private E set(ListHead<ListItem<E>> head, int index, E element) {
        checkListIndex(index, head);

        E result = get(index);

        ListHead<ListItem<E>> newHead = new ListHead<>(head, 0);
        Pair<Node<ListItem<E>>, Integer> node = bTree.copyLeaf(newHead, getTreeIndex(head, index));
        int leafIndex = node.getValue();
        Node<ListItem<E>> copy = node.getKey();

        ListItem<E> newNode = new ListItem<>(copy.getValue().get(leafIndex));
        newNode.setValue(element);
        copy.getValue().set(leafIndex, newNode);

        undo.push(newHead);
        redo.clear();
        parentUndo(element);

        return result;
    }

    private E remove(ListHead<ListItem<E>> prevHead, int index) {
        if (isFull()) {
            throw new IllegalStateException(Constants.FULL_LIST_MESSAGE);
        }

        ListHead<ListItem<E>> newHead;
        checkListIndex(index, prevHead);
        E result = get(index);

        if (prevHead.getSize() == 1) {
            undo.push(new ListHead<>());
            redo.clear();
            return result;
        }

        int treeIndex = getTreeIndex(prevHead, index);
        ListItem<E> mid = bTree.getLeaf(prevHead, treeIndex).getKey().getValue().get(treeIndex & bTree.getMask());

        int nextIndex = index + 1;
        int prevIndex = index - 1;
        newHead = new ListHead<>(prevHead, 0);

        if (mid.getPrev() == -1) {
            int treeNextIndex = getTreeIndex(nextIndex);

            bTree.copyLeaf(newHead, nextIndex);

            ListItem<E> next = getElement(newHead, nextIndex);
            ListItem<E> newNext = new ListItem<>(next);
            newNext.setPrev(-1);

            Pair<Node<ListItem<E>>, Integer> leafNext = bTree.getLeaf(newHead, treeNextIndex);
            leafNext.getKey().getValue().set(treeNextIndex & bTree.getMask(), newNext);

            newHead.setFirst(treeNextIndex);

            finishRemove(newHead);
            return result;
        }

        if (mid.getNext() == -1) {
            int treePrevIndex = getTreeIndex(prevIndex);

            bTree.copyLeaf(newHead, prevIndex);

            ListItem<E> prev = getElement(newHead, prevIndex);

            ListItem<E> newPrev = new ListItem<>(prev);
            newPrev.setNext(-1);

            Pair<Node<ListItem<E>>, Integer> leafPrev = bTree.getLeaf(newHead, treePrevIndex);
            leafPrev.getKey().getValue().set(treePrevIndex & bTree.getMask(), newPrev);

            newHead.setLast(treePrevIndex);

            finishRemove(newHead);
            return result;
        }

        int treeNextIndex = getTreeIndex(nextIndex);
        int treePrevIndex = getTreeIndex(prevIndex);

        newHead = new ListHead<>(prevHead, 0);
        bTree.copyLeaf(newHead, nextIndex);

        ListItem<E> next = getElement(newHead, nextIndex);
        ListItem<E> newNext = new ListItem<>(next);

        newNext.setPrev(mid.getPrev());

        Pair<Node<ListItem<E>>, Integer> leafNext = bTree.getLeaf(newHead, treeNextIndex);
        leafNext.getKey().getValue().set(treeNextIndex & bTree.getMask(), newNext);

        newHead = new ListHead<>(newHead, 0);
        bTree.copyLeaf(newHead, prevIndex);

        ListItem<E> prev = getElement(newHead, prevIndex);

        ListItem<E> newPrev = new ListItem<>(prev);

        newPrev.setNext(mid.getNext());

        Pair<Node<ListItem<E>>, Integer> leafPrev = bTree.getLeaf(newHead, treePrevIndex);
        leafPrev.getKey().getValue().set(treePrevIndex & bTree.getMask(), newPrev);

        if (newHead.getDeadList() == null) {
            newHead.setDeadList(new ArrayDeque<>());
        } else {
            newHead.setDeadList(new ArrayDeque<>(newHead.getDeadList()));
        }

        newHead.getDeadList().push(treeIndex);

        finishRemove(newHead);
        return result;
    }

    private void finishRemove(ListHead<ListItem<E>> newHead) {
        newHead.setSize(newHead.getSize() - 1);
        undo.push(newHead);
        redo.clear();
    }

    private void parentUndo(E value) {
        if (value instanceof PersistentLinkedList) {
            //noinspection rawtypes
            ((PersistentLinkedList) value).parent = this;
        }

        if (parent != null) {
            parent.undoValues.push(this);
        }
    }

    private int getTreeIndex(int listIndex) {
        return getTreeIndex(getCurrentHead(), listIndex);
    }

    private int getTreeIndex(ListHead<ListItem<E>> head, int listIndex) {
        checkListIndex(listIndex, head);

        if (head.getSize() == 0) {
            return -1;
        }

        int result = head.getFirst();
        ListItem<E> current;

        for (int i = 0; i < listIndex; i++) {
            Pair<Node<ListItem<E>>, Integer> pair = bTree.getLeaf(head, result);
            current = pair.getKey().getValue().get(pair.getValue());
            result = current.getNext();
        }

        return result;
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
        checkListIndex(index);
        int treeIndex = getTreeIndex(index);
        if (treeIndex == -1) {
            throw new IndexOutOfBoundsException("getTreeIndex == -1");
        }

        return bTree.getLeaf(head, treeIndex).getKey().getValue().get(treeIndex & bTree.getMask());
    }

    private ListItem<E> getValue(ListHead<ListItem<E>> head, int index) {
        return bTree.getLeaf(head, index).getKey().getValue().get(index & bTree.getMask());
    }

    private Pair<Integer, Boolean> getNextIndex(ListHead<ListItem<E>> head) {
        if (head.getDeadList() == null) {
            return new Pair<>(head.getSizeTree(), false);
        }

        if (head.getDeadList().isEmpty()) {
            return new Pair<>(head.getSizeTree(), false);
        }

        head.setDeadList(new ArrayDeque<>(head.getDeadList()));
        return new Pair<>(head.getDeadList().pop(), true);
    }

    private Object[] toArray(ListHead<ListItem<E>> head) {
        Object[] objects = new Object[head.getSize()];
        Iterator<E> iterator = iterator(head);
        for (int i = 0; i < objects.length; i++) {
            objects[i] = iterator.next();
        }
        return objects;
    }

    private void checkListIndex(int index) {
        checkListIndex(index, getCurrentHead());
    }

    private void checkListIndex(int index, ListHead<ListItem<E>> head) {
        if (index < 0 || index >= head.getSize()) {
            throw new IndexOutOfBoundsException(Constants.INVALID_INDEX_MESSAGE);
        }
    }

    private boolean isFull(ListHead<ListItem<E>> head) {
        return head.getSizeTree() >= bTree.getMaxSize();
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