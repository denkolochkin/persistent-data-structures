package ru.nsu.fit;

import org.junit.jupiter.api.Test;
import ru.nsu.fit.array.PersistentArray;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PersistentArrayTest {

    @Test
    void testPersistentArrayGet() {
        PersistentArray<String> persistentArray = new PersistentArray<>(32);
        assertEquals(0, persistentArray.size());
        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");

        assertEquals("1", persistentArray.get(0));
        assertEquals("2", persistentArray.get(1));
        assertEquals("3", persistentArray.get(2));
        assertEquals(3, persistentArray.size());
    }

    @Test
    void testPersistentArrayToArray() {
        PersistentArray<String> persistentArray = new PersistentArray<>(1);
        assertEquals(0, persistentArray.size());

        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");
        assertEquals("[1, 2, 3]", Arrays.toString(persistentArray.toArray()));
        assertEquals(3, persistentArray.size());
    }

    @Test
    void testPersistentArrayIsEmpty() {
        PersistentArray<String> persistentArray = new PersistentArray<>(1);
        assertEquals(0, persistentArray.size());

        assertTrue(persistentArray.isEmpty());
        persistentArray.add("ABOBA");
        assertFalse(persistentArray.isEmpty());
        assertEquals(1, persistentArray.size());
    }

    @Test
    void testPersistentArrayUndoRedo() {
        PersistentArray<String> persistentArray = new PersistentArray<>(1);
        assertEquals(0, persistentArray.size());

        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");

        persistentArray.undo();
        persistentArray.undo();
        assertEquals("[1]", persistentArray.toString());

        persistentArray.redo();
        assertEquals("[1, 2]", persistentArray.toString());

        persistentArray.undo();
        persistentArray.undo();
        assertEquals("[]", persistentArray.toString());

        persistentArray.redo();
        persistentArray.redo();
        persistentArray.redo();
        assertEquals("[1, 2, 3]", persistentArray.toString());
    }

    @Test
    void testPersistentArrayCascade() {
        PersistentArray<PersistentArray<PersistentArray<String>>> persistentArrayrent = new PersistentArray<>();
        PersistentArray<PersistentArray<String>> child1 = new PersistentArray<>();
        PersistentArray<PersistentArray<String>> child2 = new PersistentArray<>();
        PersistentArray<PersistentArray<String>> child3 = new PersistentArray<>();

        PersistentArray<String> child11 = new PersistentArray<>();
        PersistentArray<String> child12 = new PersistentArray<>();
        PersistentArray<String> child13 = new PersistentArray<>();

        PersistentArray<String> child21 = new PersistentArray<>();
        PersistentArray<String> child22 = new PersistentArray<>();
        PersistentArray<String> child23 = new PersistentArray<>();

        PersistentArray<String> child31 = new PersistentArray<>();
        PersistentArray<String> child32 = new PersistentArray<>();
        PersistentArray<String> child33 = new PersistentArray<>();

        PersistentArray<PersistentArray<String>> childtest;

        persistentArrayrent.add(child1);
        persistentArrayrent.add(child2);
        persistentArrayrent.add(child3);

        persistentArrayrent.get(0).add(child11);
        persistentArrayrent.get(0).add(child12);
        persistentArrayrent.get(0).add(child13);

        persistentArrayrent.get(1).add(child21);
        persistentArrayrent.get(1).add(child22);
        persistentArrayrent.get(1).add(child23);

        persistentArrayrent.get(2).add(child31);
        persistentArrayrent.get(2).add(child32);
        persistentArrayrent.get(2).add(child33);

        persistentArrayrent.get(0).get(0).add("1");
        persistentArrayrent.get(0).get(1).add("2");
        persistentArrayrent.get(0).get(2).add("3");

        persistentArrayrent.get(1).get(0).add("11");
        persistentArrayrent.get(1).get(1).add("22");
        persistentArrayrent.get(1).get(2).add("33");

        persistentArrayrent.get(2).get(0).add("111");
        persistentArrayrent.get(2).get(1).add("222");
        persistentArrayrent.get(2).get(2).add("333");
        persistentArrayrent.get(2).get(2).add("444");

        assertFalse(persistentArrayrent.contains("199"));
        assertTrue(persistentArrayrent.contains(child1));
        assertFalse(persistentArrayrent.contains(child12));

        assertEquals("[[[1], [2], [3]], [[11], [22], [33]], [[111], [222], [333, 444]]]", persistentArrayrent.toString());
        persistentArrayrent.undo();
        assertEquals("[[[1], [2], [3]], [[11], [22], [33]]]", persistentArrayrent.toString());
        persistentArrayrent.redo();
        assertEquals("[[[1], [2], [3]], [[11], [22], [33]], [[111], [222], [333, 444]]]", persistentArrayrent.toString());
        childtest = persistentArrayrent.remove(0);
        assertEquals("[[[11], [22], [33]], [[111], [222], [333, 444]]]", persistentArrayrent.toString());
        persistentArrayrent.add(0,childtest);
        assertEquals("[[[1], [2], [3]], [[11], [22], [33]], [[111], [222], [333, 444]]]", persistentArrayrent.toString());
    }

    @Test
    void testPersistentArrayIterator() {
        PersistentArray<String> persistentArray = new PersistentArray<>(4);
        assertEquals(0, persistentArray.size());

        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");

        Iterator<String> i = persistentArray.iterator();
        assertEquals("1", i.next());
        assertEquals("2", i.next());
        assertEquals("3", i.next());
        assertFalse(i.hasNext());
    }

    @Test
    void testPersistentArrayListIterator() {
        PersistentArray<String> persistentArray = new PersistentArray<>(4);
        assertEquals(0, persistentArray.size());

        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");

        ListIterator<String> i = persistentArray.listIterator();
        assertEquals("1", i.next());
        assertEquals("2", i.next());
        assertEquals("3", i.next());
        assertFalse(i.hasNext());

        assertEquals("3", i.previous());
        assertEquals("2", i.previous());
        assertEquals("1", i.previous());
        assertFalse(i.hasPrevious());

        assertTrue(i.hasNext());

        assertEquals("1", i.next());

        i.remove();

        assertEquals("[1, 3]",persistentArray.toString());

        assertEquals("3",i.next());


    }

    @Test
    void testPersistentArrayForEach() {
        PersistentArray<String> persistentArray = new PersistentArray<>(1);
        assertEquals(0, persistentArray.size());

        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");

        StringBuilder stringBuilder = new StringBuilder();
        for (String s : persistentArray) {
            stringBuilder.append(s);
        }
        assertEquals("123", stringBuilder.toString());

        stringBuilder = new StringBuilder();
        persistentArray.forEach(stringBuilder::append);
        assertEquals("123", stringBuilder.toString());
    }

    @Test
    void testPersistentArraySet() {
        PersistentArray<String> persistentArray = new PersistentArray<>(1);
        assertEquals(0, persistentArray.size());

        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");

        assertEquals("[1, 2, 3]", persistentArray.toString());
        assertEquals("1", persistentArray.set(0, "4"));
        assertEquals("2", persistentArray.set(1, "5"));
        assertEquals("[4, 5, 3]", persistentArray.toString());
        persistentArray.undo();
        persistentArray.undo();
        assertEquals("[1, 2, 3]", persistentArray.toString());
    }

    @Test
    void testPersistentArrayAddInTheMiddle() {
        PersistentArray<String> persistentArray = new PersistentArray<>(4);
        persistentArray.add("3");
        persistentArray.add("7");
        persistentArray.add("6");
        persistentArray.add("9");
        persistentArray.add("1");
        assertFalse(persistentArray.contains("200"));
        assertEquals("[3, 7, 6, 9, 1]", persistentArray.toString());
        persistentArray.add(3, "8");
        assertEquals("[3, 7, 6, 8, 9, 1]", persistentArray.toString());
        assertThrows(IndexOutOfBoundsException.class, () -> persistentArray.add(-1, "8"));
        assertThrows(IndexOutOfBoundsException.class, () -> persistentArray.add(6, "8"));
        assertThrows(IndexOutOfBoundsException.class, () -> persistentArray.add(9999, "8"));
    }

    @Test
    void testPersistentArrayToString() {
        PersistentArray<String> persistentArray = new PersistentArray<>(4);
        persistentArray.add("3");
        persistentArray.add("7");
        persistentArray.add("6");
        persistentArray.add("9");
        persistentArray.add("1");

        assertEquals("[3, 7, 6, 9, 1]", persistentArray.toString());
    }

    @Test
    void testPersistentArrayRemove() {
        PersistentArray<String> persistentArray = new PersistentArray<>(4);
        persistentArray.add("3");
        persistentArray.add("7");
        persistentArray.add("6");

        assertThrows(IndexOutOfBoundsException.class, () -> persistentArray.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> persistentArray.remove(-99999));
        assertThrows(IndexOutOfBoundsException.class, () -> persistentArray.remove(999));

        assertEquals("7", persistentArray.remove(1));
        assertEquals("[3, 6]", persistentArray.toString());

        assertEquals("6", persistentArray.remove(1));
        assertEquals("[3]", persistentArray.toString());

        assertEquals("3", persistentArray.remove(0));
        assertEquals("[]", persistentArray.toString());
        assertThrows(IndexOutOfBoundsException.class, () -> persistentArray.remove(0));
    }

    @Test
    void testPersistentArrayClear() {
        PersistentArray<String> persistentArray = new PersistentArray<>(4);
        persistentArray.add("3");
        persistentArray.add("7");
        persistentArray.add("6");
        persistentArray.add("9");
        persistentArray.add("1");

        persistentArray.clear();
        assertEquals("[]", persistentArray.toString());
        persistentArray.undo();
        assertEquals("[3, 7, 6, 9, 1]", persistentArray.toString());
    }

    @Test
    void testPersistentArrayIndexOf() {
        PersistentArray<String> persistentArray = new PersistentArray<>(32);
        assertEquals(0, persistentArray.size());
        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");
        persistentArray.add("2");
        assertEquals(2, persistentArray.indexOf("3"));
        assertEquals(1, persistentArray.indexOf("2"));
        assertEquals(0, persistentArray.indexOf("1"));
        assertEquals(-1, persistentArray.indexOf("11"));
        assertThrows(NullPointerException.class, ()->persistentArray.indexOf(null));
    }

    @Test
    void testPersistentArrayLastIndexOf() {
        PersistentArray<String> persistentArray = new PersistentArray<>(32);
        assertEquals(0, persistentArray.size());
        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");
        persistentArray.add("2");
        assertEquals(2, persistentArray.lastIndexOf("3"));
        assertEquals(3, persistentArray.lastIndexOf("2"));
        assertEquals(0, persistentArray.lastIndexOf("1"));
        assertEquals(-1, persistentArray.lastIndexOf("11"));
        assertThrows(NullPointerException.class, ()->persistentArray.indexOf(null));
    }

    @Test
    void testPersistentArrayRemoveObject() {
        PersistentArray<String> persistentArray = new PersistentArray<>(128);

        assertEquals(0, persistentArray.size());

        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");
        persistentArray.add("2");
        persistentArray.add("100");

        assertTrue(persistentArray.remove("3"));
        assertTrue(persistentArray.remove("2"));
        assertTrue(persistentArray.remove("1"));
        assertFalse(persistentArray.remove("11"));
        assertThrows(NullPointerException.class, ()-> persistentArray.remove(null));
        assertEquals(2, persistentArray.size());
        assertEquals("[2, 100]",persistentArray.toString());
    }

    @Test
    void testPersistentArrayContains() {
        PersistentArray<String> persistentArray = new PersistentArray<>(128);

        assertEquals(0, persistentArray.size());

        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");
        persistentArray.add("2");
        persistentArray.add("100");

        assertTrue(persistentArray.contains("3"));
        assertTrue(persistentArray.contains("2"));
        assertTrue(persistentArray.contains("1"));
        assertFalse(persistentArray.contains("11"));
        assertThrows(NullPointerException.class, ()-> persistentArray.contains(null));
    }

    @Test
    void testPersistentArrayContainsAll() {
        PersistentArray<String> persistentArray = new PersistentArray<>(2);

        assertEquals(0, persistentArray.size());

        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");
        persistentArray.add("2");
        persistentArray.add("100");

        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");
        persistentArray.add("2");
        persistentArray.add("100");


        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");
        persistentArray.add("2");
        persistentArray.add("100");


        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");
        persistentArray.add("2");
        persistentArray.add("100");


        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");
        persistentArray.add("2");
        persistentArray.add("100");

        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");
        persistentArray.add("2");
        persistentArray.add("100");

        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");
        persistentArray.add("2");
        persistentArray.add("100");

        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");
        persistentArray.add("2");
        persistentArray.add("100");
        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");
        persistentArray.add("2");
        persistentArray.add("100");
        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");
        persistentArray.add("2");
        persistentArray.add("100");
        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");
        persistentArray.add("2");
        persistentArray.add("100");
        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");
        persistentArray.add("2");
        persistentArray.add("100");

        Collection<String> collection = new ArrayList<>();

        collection.add("1");
        collection.add("2");
        collection.add("3");
        collection.add("100");

        assertTrue(persistentArray.containsAll(collection));

        collection.add(null);

        assertThrows(NullPointerException.class, ()-> persistentArray.containsAll(collection));

        collection.remove(null);
        collection.add("1000");

        assertFalse(persistentArray.containsAll(collection));

    }

    @Test
    void testPersistentArrayAddAll() {
        PersistentArray<String> persistentArray = new PersistentArray<>(2);

        assertEquals(0, persistentArray.size());

        persistentArray.add("1");
        persistentArray.add("2");
        persistentArray.add("3");
        persistentArray.add("2");
        persistentArray.add("100");

        Collection<String> collection = new ArrayList<>();

        collection.add("1");
        collection.add("2");
        collection.add("3");
        collection.add("100");
        collection.add("ABBBSD");

        assertTrue(persistentArray.addAll(collection));
        assertTrue(persistentArray.addAll(collection));

        assertEquals("[1, 2, 3, 2, 100, 1, 2, 3, 100, ABBBSD, 1, 2, 3, 100, ABBBSD]",persistentArray.toString());

        persistentArray.undo();

        assertEquals("[1, 2, 3, 2, 100, 1, 2, 3, 100, ABBBSD]",persistentArray.toString());

        persistentArray.undo();

        assertEquals("[1, 2, 3, 2, 100]",persistentArray.toString());

        persistentArray.redo();
        persistentArray.redo();

        assertEquals("[1, 2, 3, 2, 100, 1, 2, 3, 100, ABBBSD, 1, 2, 3, 100, ABBBSD]",persistentArray.toString());

        persistentArray.undo();
        persistentArray.undo();

        assertTrue(persistentArray.addAll(2,collection));
        assertTrue(persistentArray.addAll(2,collection));

        assertEquals("[1, 2, 1, 2, 3, 100, ABBBSD, 1, 2, 3, 100, ABBBSD, 3, 2, 100]",persistentArray.toString());

        persistentArray.undo();

        assertEquals("[1, 2, 1, 2, 3, 100, ABBBSD, 3, 2, 100]",persistentArray.toString());

        persistentArray.undo();

        assertEquals("[1, 2, 3, 2, 100]",persistentArray.toString());

        persistentArray.redo();
        persistentArray.redo();

        assertEquals("[1, 2, 1, 2, 3, 100, ABBBSD, 1, 2, 3, 100, ABBBSD, 3, 2, 100]",persistentArray.toString());

    }
}