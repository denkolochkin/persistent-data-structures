package ru.nsu.fit.list;

import lombok.Getter;
import lombok.Setter;
import ru.nsu.fit.util.BTree;
import ru.nsu.fit.util.Node;

@Getter
@Setter
public class ListHead<T> extends BTree<T> {

    private int first = -1;

    private int last = -1;

    public ListHead() {
        super(1,4);
    }

    public void copy(ListHead<T> other) {
        this.first = other.first;
        this.last = other.last;
    }

    public ListHead(ListHead<T> other) {
        super(other.getDepth(),other.getBits());
        this.setRoot(new Node<>(other.getRoot()));
        this.setSize(other.getSize());
        this.setActualSize(other.getActualSize());
        copy(other);
    }

    public boolean isEmpty() {
        return this.getSize() <= 0;
    }
}