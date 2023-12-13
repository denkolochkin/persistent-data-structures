package ru.nsu.fit.list;

import lombok.Getter;
import lombok.Setter;
import ru.nsu.fit.util.Node;

@Getter
@Setter
public class ListHead<T> {

    private int first = -1;

    private int last = -1;

    private int sizeTree = 0;

    private Node<T> root;

    private int size = 0;

    public ListHead() {
        this.root = new Node<>();
    }

    public void copy(ListHead<T> other) {
        this.root = new Node<>(other.root);
        this.first = other.first;
        this.last = other.last;
        this.sizeTree = other.sizeTree;
    }

    public ListHead(ListHead<T> other) {
        this.size = other.size;
        copy(other);
    }

    public ListHead(ListHead<T> other, Integer sizeDelta) {
        this.root = new Node<>(other.root);
        this.size = other.size + sizeDelta;
        copy(other);
    }

    public boolean isEmpty() {
        return size <= 0;
    }
}