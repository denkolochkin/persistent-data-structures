package ru.nsu.fit.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListItem<T> {

    private int nextIndex;

    private int prevIndex;

    private T value;

    public ListItem(T value, int prevIndex, int nextIndex) {
        this.nextIndex = nextIndex;
        this.prevIndex = prevIndex;
        this.value = value;
    }

    public ListItem(ListItem<T> other) {
        this.nextIndex = other.nextIndex;
        this.prevIndex = other.prevIndex;
        this.value = other.value;
    }
}
