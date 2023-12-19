package ru.nsu.fit.map;


import ru.nsu.fit.Interfaces.UndoRedoInterface;
import ru.nsu.fit.array.PersistentArray;
import ru.nsu.fit.list.PersistentLinkedList;

import java.util.*;

public class PersistentMap<K, V> extends AbstractMap<K, V> implements UndoRedoInterface {
    private static final int TABLE_MAX_SIZE = 32;

    private final PersistentArray<PersistentLinkedList<Entry<K, V>>> table;
    private final ArrayDeque<Integer> redoDeque = new ArrayDeque<>();
    private final ArrayDeque<Integer> undoDeque = new ArrayDeque<>();

    public PersistentMap() {
        this.table = new PersistentArray<>(TABLE_MAX_SIZE);
        for (int i = 0; i < TABLE_MAX_SIZE; i++) {
            table.add(new PersistentLinkedList<>());
        }
    }

    /**
     * Отмена последнего изменения.
     */
    @Override
    public void undo() {
        if (!undoDeque.isEmpty()) {
            if (undoDeque.peek().equals(TABLE_MAX_SIZE)) {
                table.undo();
                table.undo();
                redoDeque.push(undoDeque.pop());
                return;
            }
            if (undoDeque.peek() > TABLE_MAX_SIZE) {
                Integer peek = undoDeque.pop();
                for (int i = 0; i < peek - TABLE_MAX_SIZE; i++) {
                    table.get(undoDeque.peek()).undo();
                    redoDeque.push(undoDeque.pop());
                }
                redoDeque.push(peek);
                return;
            }
            table.get(undoDeque.peek()).undo();
            redoDeque.push(undoDeque.pop());
        }
    }

    /**
     * Отмена последнего undo().
     */
    @Override
    public void redo() {
        if (!redoDeque.isEmpty()) {
            if (redoDeque.peek().equals(TABLE_MAX_SIZE)) {
                table.redo();
                table.redo();
                undoDeque.push(redoDeque.pop());
                return;
            }
            if (redoDeque.peek() > TABLE_MAX_SIZE) {
                Integer peek = redoDeque.pop();
                for (int i = 0; i < peek - TABLE_MAX_SIZE; i++) {
                    table.get(redoDeque.peek()).redo();
                    undoDeque.push(redoDeque.pop());
                }
                undoDeque.push(peek);
                return;
            }
            table.get(redoDeque.peek()).redo();
            undoDeque.push(redoDeque.pop());
        }
    }

    /**
     * Вставка пары ключ-значение.
     *
     * @param key ключ.
     * @param value значение.
     * @return вставленное значение.
     */
    @Override
    public V put(K key, V value) {
        V result = get(key);

        int index = hashcodeIndex(key.hashCode());
        for (int i = 0; i < table.get(index).size(); i++) {
            Entry<K, V> pair = table.get(index).get(i);
            if (pair.getKey().equals(key)) {
                table.get(index).set(i, new SimpleEntry<>(key, value));
                updateUndoRedoStack(index);

                return result;
            }
        }

        table.get(index).add(new SimpleEntry<>(key, value));
        updateUndoRedoStack(index);

        return result;
    }

    /**
     * Вставка всех элементов из m.
     *
     * @param m Map с аналогично параметризованными ключами и значениями.
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }

        updateUndoRedoStack(TABLE_MAX_SIZE + m.size());
    }

    /**
     * Удаление значения по ключу.
     *
     * @param key ключ.
     * @return удаленное значение.
     */
    @Override
    public V remove(Object key) {
        int index = hashcodeIndex(key.hashCode());
        for (int i = 0; i < table.get(index).size(); i++) {
            Entry<K, V> pair = table.get(index).get(i);
            if (pair.getKey().equals(key)) {
                V value = pair.getValue();
                table.get(index).remove(i);
                updateUndoRedoStack(index);

                return value;
            }
        }
        return null;
    }

    /**
     * Удаление всех элементов коллекции.
     */
    @Override
    public void clear() {
        Collection<PersistentLinkedList<Entry<K, V>>> newArray = new ArrayList<>();
        for (int i = 0; i < TABLE_MAX_SIZE; i++) {
            newArray.add(new PersistentLinkedList<>());
        }
        table.clear();
        updateUndoRedoStack(TABLE_MAX_SIZE);

        table.addAll(newArray);
    }

    /**
     * Получение значения по ключу.
     *
     * @param key ключ, ассоциированный с искомым значением.
     * @return значение, если было найдено по ключу, иначе - null.
     */
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

    /**
     * Получение множества ключей.
     *
     * @return Set ключей.
     */
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

    /**
     * Получение множества Entry.
     *
     * @return Set из всех Entry.
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entrySet = new HashSet<>();
        for (PersistentLinkedList<Entry<K, V>> pairs : table) {
            entrySet.addAll(pairs);
        }
        return entrySet;
    }

    /**
     * Получение множества значений.
     *
     * @return Set значений.
     */
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

    /**
     * Преобразование элементов в строку.
     *
     * @return строка из элементов вида {{k1, v1}, {k2, v2}, ... }.
     */
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

    private void updateUndoRedoStack(int index){
        undoDeque.push(index);
        redoDeque.clear();
    }
}