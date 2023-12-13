package ru.nsu.fit.util;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Node<E> {
    private List<E> value;
    private List<Node<E>> child;

    public Node() {
    }

    public Node(Node<E> other) {
        if (other != null) {
            if (other.child != null) {
                child = new ArrayList<>();
                child.addAll(other.child);
            }

            if (other.value != null) {
                value = new ArrayList<>();
                value.addAll(other.value);
            }
        }
    }

    public Node(Node<E> other, int endIndex) {
        if (other.child != null) {
            child = new ArrayList<>();
            for (int i = 0; i <= endIndex; i++) {
                child.add(other.child.get(i));
            }
        }

        if (other.value != null) {
            value = new ArrayList<>();
            for (int i = 0; i <= endIndex; i++) {
                value.add(other.value.get(i));
            }
        }
    }
}