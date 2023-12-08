package ru.nsu.fit.list;

import lombok.Getter;
import lombok.Setter;
import ru.nsu.fit.util.node.Node;

@Getter
@Setter
public class Head<T> {

    private final Node<T> root;

    private int size = 0;

    public Head() {
        this.root = new Node<>();
    }

    public Head(Head<T> other) {
        this.root = new Node<>(other.root);
        this.size = other.size;
    }

    public Head(Head<T> other, Integer sizeDelta) {
        this.root = new Node<>(other.root);
        this.size = other.size + sizeDelta;
    }
}
