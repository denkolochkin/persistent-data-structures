package ru.nsu.fit;

import org.junit.jupiter.api.Test;
import ru.nsu.fit.map.PersistentMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PersistentMapTest {

    @Test
    void testPersistentHashMapGet() {
        PersistentMap<String, String> persistentMap = new PersistentMap<>();

        persistentMap.put("key1", "1");
        persistentMap.put("key2", "2");
        persistentMap.put("key3", "3");

        assertEquals("1", persistentMap.get("key1"));
        assertEquals("2", persistentMap.get("key2"));
        assertEquals("3", persistentMap.get("key3"));
    }

    @Test
    void testPersistentHashMapKeySet() {
        PersistentMap<String, String> persistentMap = new PersistentMap<>();

        persistentMap.put("key1", "1");
        persistentMap.put("key2", "2");
        persistentMap.put("key3", "3");

        HashSet<String> hashSet = new HashSet<>();
        hashSet.add("key1");
        hashSet.add("key2");
        hashSet.add("key3");

        assertEquals(hashSet, persistentMap.keySet());
    }

    @Test
    void testPersistentHashMapUndoRedo() {
        PersistentMap<String, String> persistentMap = new PersistentMap<>();

        persistentMap.put("key1", "1");
        persistentMap.put("key2", "2");
        persistentMap.put("key3", "3");

        persistentMap.undo();
        assertEquals("1", persistentMap.get("key1"));
        assertEquals("2", persistentMap.get("key2"));
        assertFalse(persistentMap.containsKey("key3"));

        persistentMap.undo();
        assertEquals("1", persistentMap.get("key1"));
        assertFalse(persistentMap.containsKey("key2"));
        assertFalse(persistentMap.containsKey("key3"));

        persistentMap.redo();
        assertEquals("1", persistentMap.get("key1"));
        assertEquals("2", persistentMap.get("key2"));
        assertFalse(persistentMap.containsKey("key3"));

        persistentMap.redo();
        assertEquals("1", persistentMap.get("key1"));
        assertEquals("2", persistentMap.get("key2"));
        assertEquals("3", persistentMap.get("key3"));

        assertEquals(3, persistentMap.size());

        persistentMap.undo();
        persistentMap.undo();
        persistentMap.undo();
        assertEquals(0, persistentMap.size());
    }

    @Test
    void testPersistentHashMapContainsKey() {
        PersistentMap<String, String> persistentMap = new PersistentMap<>();

        persistentMap.put("key1", "1");
        persistentMap.put("key2", "2");
        persistentMap.put("key3", "3");

        assertEquals(3, persistentMap.size());

        assertTrue(persistentMap.containsKey("key1"));
        assertTrue(persistentMap.containsKey("key2"));
        assertTrue(persistentMap.containsKey("key3"));
        assertFalse(persistentMap.containsKey("key4"));

    }

    @Test
    void testPersistentHashMapContainsValue() {
        PersistentMap<String, String> persistentMap = new PersistentMap<>();

        persistentMap.put("key1", "1");
        persistentMap.put("key2", "2");
        persistentMap.put("key3", "3");

        assertEquals(3, persistentMap.size());

        assertTrue(persistentMap.containsValue("1"));
        assertTrue(persistentMap.containsValue("2"));
        assertTrue(persistentMap.containsValue("3"));

        assertFalse(persistentMap.containsValue("key1"));
    }


    @Test
    void testPersistentHashMapClear() {
        PersistentMap<String, String> persistentMap = new PersistentMap<>();

        persistentMap.put("key1", "1");
        persistentMap.put("key2", "2");
        persistentMap.put("key3", "3");

        assertEquals(3, persistentMap.size());
        persistentMap.clear();

        assertTrue(persistentMap.isEmpty());

        persistentMap.undo();

        assertEquals(3, persistentMap.size());

    }

    @Test
    void testPersistentHashMapRemove() {
        PersistentMap<String, String> persistentMap = new PersistentMap<>();

        persistentMap.put("key1", "1");
        persistentMap.put("key2", "2");
        persistentMap.put("key3", "3");

        assertEquals(3, persistentMap.size());

        persistentMap.remove("key2");
        assertFalse(persistentMap.containsKey("key2"));
        assertEquals(2, persistentMap.size());

        persistentMap.remove("key3");
        assertFalse(persistentMap.containsKey("key3"));
        assertEquals(1, persistentMap.size());

        persistentMap.undo();
        persistentMap.undo();

        assertEquals(3, persistentMap.size());
    }


    @Test
    void testPersistentHashMapCascade() {
        PersistentMap<String, PersistentMap<String, String>> persistentMap = new PersistentMap<>();

        PersistentMap<String, String> persistentMap1 = new PersistentMap<>();
        PersistentMap<String, String> persistentMap2 = new PersistentMap<>();
        PersistentMap<String, String> persistentMap3 = new PersistentMap<>();

        persistentMap.put("SuperKey1", persistentMap1);
        persistentMap.put("SuperKey2", persistentMap2);
        persistentMap.put("SuperKey3", persistentMap3);

        persistentMap.get("SuperKey1").put("key1", "11");
        persistentMap.get("SuperKey1").put("key2", "12");
        persistentMap.get("SuperKey1").put("key3", "13");

        persistentMap.get("SuperKey2").put("key1", "21");
        persistentMap.get("SuperKey2").put("key2", "22");
        persistentMap.get("SuperKey2").put("key3", "23");

        persistentMap.get("SuperKey3").put("key1", "31");
        persistentMap.get("SuperKey3").put("key2", "32");
        persistentMap.get("SuperKey3").put("key3", "33");
        persistentMap.get("SuperKey3").put("key4", "34");

        assertEquals(3, persistentMap.size());
        assertEquals(3, persistentMap.get("SuperKey1").size());
        assertEquals(3, persistentMap.get("SuperKey2").size());
        assertEquals(4, persistentMap.get("SuperKey3").size());

        assertEquals(persistentMap1, persistentMap.get("SuperKey1"));
        assertEquals("31", persistentMap.get("SuperKey3").get("key1"));
        persistentMap.clear();

        assertTrue(persistentMap.isEmpty());

        persistentMap.undo();

        assertEquals(3, persistentMap.size());

        assertEquals(persistentMap2, persistentMap.get("SuperKey2"));
        assertEquals("23", persistentMap.get("SuperKey2").get("key3"));
    }

    @Test
    void testPersistentHashMapPutAll() {
        PersistentMap<String, String> persistentMap = new PersistentMap<>();

        persistentMap.put("key1", "1");
        persistentMap.put("key2", "2");
        persistentMap.put("key3", "3");

        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("key4", "4");
        hashMap.put("key5", "5");
        hashMap.put("key6", "6");
        hashMap.put("key7", "7");
        hashMap.put("key8", "8");
        hashMap.put("key9", "9");

        assertEquals(3, persistentMap.size());

        persistentMap.putAll((hashMap));

        assertEquals(9, persistentMap.size());

        assertEquals("8", persistentMap.get("key8"));

        persistentMap.undo();

        assertNull(persistentMap.get("key8"));

        assertEquals(3, persistentMap.size());

        persistentMap.redo();

        assertEquals("7", persistentMap.get("key7"));

        assertEquals(9, persistentMap.size());
    }

    @Test
    void largeInsertTest() {
        int toStore = 1000;
        PersistentMap<Integer, Integer> persistentMap = new PersistentMap<>();

        for (int i = 0; i < toStore; i++) {
            persistentMap.put(i, i);
        }

        assertEquals(toStore, persistentMap.size());

        for (int i = 0; i < toStore; i++) {
            assertEquals(i, persistentMap.get(i));
        }

        for (int i = 0; i < toStore; i++) {
            persistentMap.undo();
        }

        assertTrue(persistentMap.isEmpty());

        for (int i = 0; i < toStore; i++) {
            persistentMap.redo();
        }

        assertEquals(toStore, persistentMap.size());

        for (int i = 0; i < toStore; i++) {
            assertEquals(i, persistentMap.get(i));
        }

        assertEquals(toStore, persistentMap.size());

        for (int i = 0; i < toStore; i++) {
            assertEquals(i, persistentMap.remove(i));
        }

        assertTrue(persistentMap.isEmpty());

        for (int i = 0; i < toStore; i++) {
            persistentMap.undo();
        }
    }

}