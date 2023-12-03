package ru.nsu.fit.list;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListItem<T> {

    private int next;

    private int prev;

    private T value;

    public ListItem(T value, int prev, int next) {
        this.next = next;
        this.prev = prev;
        this.value = value;
    }

    public ListItem(ListItem<T> other) {
        this.next = other.next;
        this.prev = other.prev;
        this.value = other.value;
    }
}
