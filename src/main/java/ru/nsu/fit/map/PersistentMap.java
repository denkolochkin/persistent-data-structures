package ru.nsu.fit.map;


import ru.nsu.fit.Interfaces.UndoRedoInterface;
import ru.nsu.fit.array.PersistentArray;
import ru.nsu.fit.list.PersistentLinkedList;

import java.util.*;

public class PersistentMap<K, V> extends AbstractMap<K, V> implements UndoRedoInterface {
    private static final int TABLE_MAX_SIZE = 32;

    private final PersistentArray<PersistentLinkedList<Entry<K, V>>> table;
    private final Stack<Integer> redoStack = new Stack<>();
    private final Stack<Integer> undoStack = new Stack<>();

    public PersistentMap() {
        this.table = new PersistentArray<>(TABLE_MAX_SIZE);
        for (int i = 0; i < TABLE_MAX_SIZE; i++) {
            table.add(new PersistentLinkedList<>());
        }
    }

    @Override
    public void undo() {
        if (!undoStack.empty()) {
            if (undoStack.peek().equals(TABLE_MAX_SIZE)) {
                table.undo();
                table.undo();
                redoStack.push(undoStack.pop());
                return;
            }
            if (undoStack.peek() > TABLE_MAX_SIZE) {
                Integer peek = undoStack.pop();
                for (int i = 0; i < peek - TABLE_MAX_SIZE; i++) {
                    table.get(undoStack.peek()).undo();
                    redoStack.push(undoStack.pop());
                }
                redoStack.push(peek);
                return;
            }
            table.get(undoStack.peek()).undo();
            redoStack.push(undoStack.pop());
        }
    }

    @Override
    public void redo() {
        if (!redoStack.empty()) {
            if (redoStack.peek().equals(TABLE_MAX_SIZE)) {
                table.redo();
                table.redo();
                undoStack.push(redoStack.pop());
                return;
            }
            if (redoStack.peek() > TABLE_MAX_SIZE) {
                Integer peek = redoStack.pop();
                for (int i = 0; i < peek - TABLE_MAX_SIZE; i++) {
                    table.get(redoStack.peek()).redo();
                    undoStack.push(redoStack.pop());
                }
                undoStack.push(peek);
                return;
            }
            table.get(redoStack.peek()).redo();
            undoStack.push(redoStack.pop());
        }
    }

    @Override
    public V put(K key, V value) {
        V result = get(key);

        int index = hashcodeIndex(key.hashCode());
        for (int i = 0; i < table.get(index).size(); i++) {
            Entry<K, V> pair = table.get(index).get(i);
            if (pair.getKey().equals(key)) {
                table.get(index).set(i, new SimpleEntry<>(key, value));
                undoStack.push(index);
                redoStack.clear();

                return result;
            }
        }

        table.get(index).add(new SimpleEntry<>(key, value));
        undoStack.push(index);
        redoStack.clear();

        return result;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }

        undoStack.push(TABLE_MAX_SIZE + m.size());
        redoStack.clear();
    }

    @Override
    public V remove(Object key) {
        int index = hashcodeIndex(key.hashCode());
        for (int i = 0; i < table.get(index).size(); i++) {
            Entry<K, V> pair = table.get(index).get(i);
            if (pair.getKey().equals(key)) {
                V value = pair.getValue();
                table.get(index).remove(i);
                undoStack.push(index);
                redoStack.clear();

                return value;
            }
        }
        return null;
    }

    @Override
    public void clear() {
        Collection<PersistentLinkedList<Entry<K, V>>> newArray = new ArrayList<>();
        for (int i = 0; i < TABLE_MAX_SIZE; i++) {
            newArray.add(new PersistentLinkedList<>());
        }
        table.clear();
        undoStack.add(TABLE_MAX_SIZE);
        redoStack.clear();

        table.addAll(newArray);
    }

    @Override
    public V get(Object key) {
        int index = hashcodeIndex(key.hashCode());
        PersistentLinkedList<Entry<K, V>> get = table.get(index);
        for (Entry<K, V> pair : get) {
            if (pair.getKey().equals(key)) {
                return pair.getValue();
            }
        }
        return null;
    }

    @Override
    public Set<K> keySet() {
        Set<K> keySet = new HashSet<>();
        for (PersistentLinkedList<Entry<K, V>> pairs : table) {
            for (Entry<K, V> pair : pairs) {
                keySet.add(pair.getKey());
            }
        }
        return keySet;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entrySet = new HashSet<>();
        for (PersistentLinkedList<Entry<K, V>> pairs : table) {
            entrySet.addAll(pairs);
        }
        return entrySet;
    }

    @Override
    public List<V> values() {
        List<V> values = new LinkedList<>();
        for (PersistentLinkedList<Entry<K, V>> pairs : table) {
            for (Entry<K, V> pair : pairs) {
                values.add(pair.getValue());
            }
        }
        return values;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "{}";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        for (Map.Entry<K, V> entry : this.entrySet()) {
            stringBuilder.append(entry);
            stringBuilder.append(", ");
        }
        stringBuilder.delete(stringBuilder.lastIndexOf(", "), stringBuilder.lastIndexOf(", ") + 2);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    private int hashcodeIndex(int hashcode) {
        return hashcode & (TABLE_MAX_SIZE - 1);
    }
}