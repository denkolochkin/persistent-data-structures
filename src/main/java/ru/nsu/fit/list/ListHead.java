package ru.nsu.fit.list;

import lombok.Getter;
import lombok.Setter;

import java.util.Deque;

@Getter
@Setter
public class ListHead<T> extends Head<T> {

    private int first = -1;

    private int last = -1;

    private int sizeTree = 0;

    private Deque<Integer> deadList;

    public ListHead() {
        super();
    }

    public void copy(ListHead<T> other) {
        this.first = other.first;
        this.last = other.last;
        this.sizeTree = other.sizeTree;
        this.deadList = other.deadList;
    }

    public ListHead(ListHead<T> other) {
        super(other);
        copy(other);
    }

    public ListHead(ListHead<T> other, Integer sizeDelta) {
        super(other, sizeDelta);
        copy(other);
    }
}