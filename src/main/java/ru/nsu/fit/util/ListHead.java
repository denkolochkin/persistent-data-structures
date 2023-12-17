package ru.nsu.fit.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListHead<T> extends BTree<T> {

    private int firstIndex = -1;

    private int lastIndex = -1;

    public ListHead() {
        super(1, 4);
    }

    public void copy(ListHead<T> other) {
        this.firstIndex = other.firstIndex;
        this.lastIndex = other.lastIndex;
    }

    public ListHead(ListHead<T> other) {
        super(other);
        copy(other);
    }

    public boolean isEmpty() {
        return this.getSize() <= 0;
    }
}