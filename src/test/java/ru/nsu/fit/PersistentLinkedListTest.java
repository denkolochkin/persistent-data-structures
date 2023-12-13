package ru.nsu.fit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nsu.fit.list.PersistentLinkedList;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class PersistentLinkedListTest {

    PersistentLinkedList<Integer> persistentLinkedList;

    @BeforeEach
    public void init() {
        persistentLinkedList = new PersistentLinkedList<>();
    }

    @Test
    void addTest() {
        persistentLinkedList.add(3);

        assertEquals(2, persistentLinkedList.getVersionCount());
        assertEquals(0, persistentLinkedList.getCurrentHead().getFirst());
        assertEquals(0, persistentLinkedList.getCurrentHead().getLast());
        assertEquals(1, persistentLinkedList.size());
        assertEquals("[3]", persistentLinkedList.toString());

        persistentLinkedList.add(4);

        assertEquals(3, persistentLinkedList.getVersionCount());
        assertEquals(0, persistentLinkedList.getCurrentHead().getFirst());
        assertEquals(1, persistentLinkedList.getCurrentHead().getLast());
        assertEquals(2, persistentLinkedList.size());
        assertEquals("[3, 4]", persistentLinkedList.toString());

        persistentLinkedList.add(6);

        assertEquals(4, persistentLinkedList.getVersionCount());
        assertEquals(0, persistentLinkedList.getCurrentHead().getFirst());
        assertEquals(2, persistentLinkedList.getCurrentHead().getLast());
        assertEquals(3, persistentLinkedList.size());
        assertEquals("[3, 4, 6]", persistentLinkedList.toString());

        persistentLinkedList.add(9);

        assertEquals(5, persistentLinkedList.getVersionCount());
        assertEquals(0, persistentLinkedList.getCurrentHead().getFirst());
        assertEquals(3, persistentLinkedList.getCurrentHead().getLast());
        assertEquals(4, persistentLinkedList.size());
        assertEquals("[3, 4, 6, 9]", persistentLinkedList.toString());
    }

    @Test
    void insertTest() {
        persistentLinkedList.add(3);
        persistentLinkedList.add(4);
        persistentLinkedList.add(6);

        assertEquals(4, persistentLinkedList.getVersionCount());
        assertEquals(0, persistentLinkedList.getCurrentHead().getFirst());
        assertEquals(2, persistentLinkedList.getCurrentHead().getLast());
        assertEquals(3, persistentLinkedList.size());
        assertEquals("[3, 4, 6]", persistentLinkedList.toString());

        persistentLinkedList.add(1, 9);

        assertEquals(5, persistentLinkedList.getVersionCount());
        assertEquals(0, persistentLinkedList.getCurrentHead().getFirst());
        assertEquals(2, persistentLinkedList.getCurrentHead().getLast());
        assertEquals(4, persistentLinkedList.size());
        assertEquals("[3, 9, 4, 6]", persistentLinkedList.toString());

        persistentLinkedList.add(1, 7);

        assertEquals(6, persistentLinkedList.getVersionCount());
        assertEquals(0, persistentLinkedList.getCurrentHead().getFirst());
        assertEquals(2, persistentLinkedList.getCurrentHead().getLast());
        assertEquals(5, persistentLinkedList.size());
        assertEquals("[3, 7, 9, 4, 6]", persistentLinkedList.toString());

        persistentLinkedList.add(8);

        assertEquals(7, persistentLinkedList.getVersionCount());
        assertEquals(0, persistentLinkedList.getCurrentHead().getFirst());
        assertEquals(5, persistentLinkedList.getCurrentHead().getLast());
        assertEquals(6, persistentLinkedList.size());
        assertEquals("[3, 7, 9, 4, 6, 8]", persistentLinkedList.toString());
    }

    @Test
    void beginEndInsertTest() {
        persistentLinkedList.add(1);
        persistentLinkedList.add(2);
        persistentLinkedList.add(3);

        assertEquals(4, persistentLinkedList.getVersionCount());
        assertEquals(0, persistentLinkedList.getCurrentHead().getFirst());
        assertEquals(2, persistentLinkedList.getCurrentHead().getLast());
        assertEquals(3, persistentLinkedList.size());
        assertEquals("[1, 2, 3]", persistentLinkedList.toString());

        persistentLinkedList.add(0, 4);

        assertEquals(5, persistentLinkedList.getVersionCount());
        assertEquals(3, persistentLinkedList.getCurrentHead().getFirst());
        assertEquals(2, persistentLinkedList.getCurrentHead().getLast());
        assertEquals(4, persistentLinkedList.size());
        assertEquals("[4, 1, 2, 3]", persistentLinkedList.toString());

        persistentLinkedList.add(0, 5);

        assertEquals(6, persistentLinkedList.getVersionCount());
        assertEquals(4, persistentLinkedList.getCurrentHead().getFirst());
        assertEquals(2, persistentLinkedList.getCurrentHead().getLast());
        assertEquals(5, persistentLinkedList.size());
        assertEquals("[5, 4, 1, 2, 3]", persistentLinkedList.toString());

        assertThrows(IndexOutOfBoundsException.class, () -> persistentLinkedList.add(5, 6));
    }

    @Test
    void iteratorTest() {
        persistentLinkedList.add(0);
        persistentLinkedList.add(1);
        persistentLinkedList.add(2);

        Iterator<Integer> i = persistentLinkedList.iterator();

        assertTrue(i.hasNext());
        assertEquals(Integer.valueOf(0), i.next());
        assertEquals(Integer.valueOf(1), i.next());
        assertEquals(Integer.valueOf(2), i.next());
        assertFalse(i.hasNext());

        persistentLinkedList.add(3);

        assertFalse(i.hasNext());

        i = persistentLinkedList.iterator();

        assertTrue(i.hasNext());
        assertEquals(Integer.valueOf(0), i.next());
        assertEquals(Integer.valueOf(1), i.next());
        assertEquals(Integer.valueOf(2), i.next());
        assertEquals(Integer.valueOf(3), i.next());
        assertFalse(i.hasNext());

        persistentLinkedList = new PersistentLinkedList<>();
        persistentLinkedList.add(3);
        persistentLinkedList.add(4);
        persistentLinkedList.remove(0);

        assertEquals("[4]", persistentLinkedList.toString());

        i = persistentLinkedList.iterator();

        assertTrue(i.hasNext());
        assertEquals(Integer.valueOf(4), i.next());
        assertFalse(i.hasNext());
    }

    @Test
    void removeTest() {
        persistentLinkedList.add(0);
        persistentLinkedList.add(1);
        persistentLinkedList.add(2);

        assertEquals("[0, 1, 2]", persistentLinkedList.toString());

        persistentLinkedList.remove(0);

        assertEquals("[1, 2]", persistentLinkedList.toString());

        persistentLinkedList.remove(0);

        assertEquals("[2]", persistentLinkedList.toString());
        assertEquals(1, persistentLinkedList.size());

        persistentLinkedList.add(3);
        persistentLinkedList.add(4);
        persistentLinkedList.add(5);

        assertEquals("[2, 3, 4, 5]", persistentLinkedList.toString());

        persistentLinkedList.remove(2);

        assertEquals("[2, 3, 5]", persistentLinkedList.toString());
        assertEquals(3, persistentLinkedList.size());
        assertThrows(IndexOutOfBoundsException.class, () -> persistentLinkedList.set(4123, 1));
    }

    @Test
    void setTest() {
        persistentLinkedList.add(0);
        persistentLinkedList.add(1);
        persistentLinkedList.add(2);

        assertEquals("[0, 1, 2]", persistentLinkedList.toString());
        assertEquals(1, persistentLinkedList.set(1, -1));
        assertEquals("[0, -1, 2]", persistentLinkedList.toString());

        persistentLinkedList.set(2, -2);

        assertEquals("[0, -1, -2]", persistentLinkedList.toString());

        assertThrows(IndexOutOfBoundsException.class, () -> persistentLinkedList.set(2344, 0));
    }

    @Test
    void removeLastTest() {
        persistentLinkedList.add(0);
        persistentLinkedList.add(1);

        assertEquals("[0, 1]", persistentLinkedList.toString());

        persistentLinkedList.remove(1);

        assertEquals("[0]", persistentLinkedList.toString());

        persistentLinkedList.add(2);

        assertEquals("[0, 2]", persistentLinkedList.toString());
    }

    @Test
    void removeMiddleTest() {
        persistentLinkedList.add(0);
        persistentLinkedList.add(1);
        persistentLinkedList.add(2);

        assertEquals("[0, 1, 2]", persistentLinkedList.toString());

        persistentLinkedList.remove(1);

        assertEquals("[0, 2]", persistentLinkedList.toString());

        persistentLinkedList.set(1, 9);

        assertEquals("[0, 9]", persistentLinkedList.toString());
    }

    @Test
    void clearTest() {
        persistentLinkedList.add(0);
        persistentLinkedList.add(1);
        persistentLinkedList.add(2);

        assertEquals("[0, 1, 2]", persistentLinkedList.toString());
        assertEquals(3, persistentLinkedList.size());

        persistentLinkedList.clear();

        persistentLinkedList.add(1);
        persistentLinkedList.add(2);
        persistentLinkedList.add(3);

        assertEquals("[1, 2, 3]", persistentLinkedList.toString());
        assertEquals(3, persistentLinkedList.size());
    }

    @Test
    void undoRedoTest() {
        persistentLinkedList.add(0);
        persistentLinkedList.add(1);
        persistentLinkedList.add(2);

        assertEquals("[0, 1, 2]", persistentLinkedList.toString());

        persistentLinkedList.add(3);

        assertEquals("[0, 1, 2, 3]", persistentLinkedList.toString());

        persistentLinkedList.undo();

        assertEquals("[0, 1, 2]", persistentLinkedList.toString());

        persistentLinkedList.redo();

        assertEquals("[0, 1, 2, 3]", persistentLinkedList.toString());

        persistentLinkedList.set(1, -1);

        assertEquals("[0, -1, 2, 3]", persistentLinkedList.toString());

        persistentLinkedList.undo();

        assertEquals("[0, 1, 2, 3]", persistentLinkedList.toString());

        persistentLinkedList.redo();

        assertEquals("[0, -1, 2, 3]", persistentLinkedList.toString());

        persistentLinkedList.remove(2);

        assertEquals("[0, -1, 3]", persistentLinkedList.toString());

        persistentLinkedList.undo();

        assertEquals("[0, -1, 2, 3]", persistentLinkedList.toString());

        persistentLinkedList.redo();

        assertEquals("[0, -1, 3]", persistentLinkedList.toString());
    }

    @Test
    void removeAllAndUndoRedoTest() {
        persistentLinkedList.add(0);
        persistentLinkedList.add(1);
        persistentLinkedList.add(2);

        assertEquals("[0, 1, 2]", persistentLinkedList.toString());

        persistentLinkedList.remove(0);
        persistentLinkedList.remove(0);
        persistentLinkedList.remove(0);

        assertTrue(persistentLinkedList.isEmpty());

        persistentLinkedList.undo();
        persistentLinkedList.undo();
        persistentLinkedList.undo();

        assertEquals("[0, 1, 2]", persistentLinkedList.toString());

        persistentLinkedList.remove(0);
        persistentLinkedList.remove(0);
        persistentLinkedList.remove(0);

        assertTrue(persistentLinkedList.isEmpty());

        persistentLinkedList.add(-1);
        persistentLinkedList.add(-2);
        persistentLinkedList.add(-3);

        assertEquals("[-1, -2, -3]", persistentLinkedList.toString());

        persistentLinkedList.remove(0);
        persistentLinkedList.remove(0);
        persistentLinkedList.remove(0);

        persistentLinkedList.undo();
        persistentLinkedList.undo();
        persistentLinkedList.undo();
        persistentLinkedList.redo();
        persistentLinkedList.redo();
        persistentLinkedList.redo();

        assertTrue(persistentLinkedList.isEmpty());
    }

    @Test
    void copyConstructorTest() {
        persistentLinkedList.add(0);
        persistentLinkedList.add(1);
        persistentLinkedList.add(2);

        PersistentLinkedList<Integer> newList = new PersistentLinkedList<>(persistentLinkedList);

        assertEquals(persistentLinkedList.toString(), newList.toString());
        assertEquals(persistentLinkedList.size(), newList.size());
        assertEquals(persistentLinkedList.getVersionCount(), newList.getVersionCount());
        assertEquals(persistentLinkedList.getCurrentHead(), newList.getCurrentHead());
    }
}
