package ru.nsu.fit.util;

import javafx.util.Pair;
import lombok.Getter;
import ru.nsu.fit.list.ListHead;
import ru.nsu.fit.list.ListItem;

import java.util.ArrayList;
import java.util.LinkedList;

@Getter
public class BTree<E> {
    private int depth;
    private int mask;
    private int maxSize;
    private int bits = 2;
    private int width;
    private final Node<E> root;
    private int size = 0;

    public BTree() {
        initialization(1, 4);
        this.root = new Node<>();
    }

    public BTree(int size) {
        initialization((int) Math.ceil(Math.log(size) / Math.log( (int) Math.pow(2, bits))), bits);
        this.root = new Node<>();
    }

    public BTree(int depth, int bits) {
        this.depth = depth;
        this.bits = bits;

        initialization((int) Math.ceil(Math.log(size) / Math.log( (int) Math.pow(2, bits))), bits);

        this.root = new Node<>();
    }

    public BTree(BTree<E> other) {
        initialization(other.depth,other.bits);
        this.root = new Node<>(other.root);
        this.size = other.size;
        this.width = other.width;
        this.mask = other.mask;
    }

    public BTree(BTree<E> other, Integer newSize) {
        initialization(other.depth,other.bits);
        this.root = other.createSubTree(newSize);
        this.size = newSize;
    }

    protected void initialization(int depth, int bits) {
        this.bits = bits;
        if(depth > 0) {
            this.depth = depth;
        }else{
            this.depth = 1;
        }

        updateInformationAboutTree();
    }

    private void updateInformationAboutTree(){
        mask = (int) Math.pow(2, bits) - 1;
        maxSize = (int) Math.pow(2, bits * depth);
        width = (int) Math.pow(2, bits);
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return String.format("%20x %d", root.hashCode(), size);
    }

    private void increaseDepthOfTree(){
        Node<E> newNode = new Node<>();
        if(root.getValue() == null) {
            newNode.setChild(root.getChild());
        }else{
            newNode.setValue(root.getValue());
            root.setValue(null);
        }
        root.setChild(new LinkedList<>());
        root.getChild().add(newNode);
        depth++;

        updateInformationAboutTree();
    }

    public boolean add(E element) {
        size++;

        if(size > maxSize){
            increaseDepthOfTree();
        }

        Node<E> foundNode = root;

        for (int level = bits * (depth - 1); level > 0; level -= bits) {
            int widthIndex = ((size - 1) >> level) & mask;
            Node<E> newNode;

            if (foundNode.getChild() == null) {
                foundNode.setChild(new LinkedList<>());
                newNode = new Node<>();
                foundNode.getChild().add(newNode);
            } else {
                if (widthIndex == foundNode.getChild().size()) {
                    newNode = new Node<>();
                    foundNode.getChild().add(newNode);
                } else {
                    Node<E> childNode = foundNode.getChild().get(widthIndex);
                    newNode = new Node<>(childNode);
                    foundNode.getChild().set(widthIndex, newNode);
                }
            }

            foundNode = newNode;
        }

        if (foundNode.getValue() == null) {
            foundNode.setValue(new ArrayList<>());
        }

        foundNode.getValue().add(element);

        return true;
    }

    public boolean add(int index, E element){
        findNode(index).getValue().set(index & mask, element);

        return true;
    }

    public void set(int index, E element) {
        Node<E> foundNode = root;

        for (int level = bits * (depth - 1); level > 0; level -= bits) {
            int widthIndex = (index >> level) & mask;
            Node<E> childNode = foundNode.getChild().get(widthIndex);
            Node<E> newNode = new Node<>(childNode);
            foundNode.getChild().set(widthIndex, newNode);
            foundNode = newNode;
        }

        foundNode.getValue().set(index & mask, element);
    }

    public Node<ListItem<E>> findLeaf(ListHead<ListItem<E>> head) {
        head.setSize(head.getSize() + 1);
        head.setSizeTree(head.getSizeTree() + 1);

        Node<ListItem<E>> currentNode = head.getRoot();
        for (int level = bits * (depth - 1); level > 0; level -= bits) {
            int widthIndex = ((head.getSizeTree() - 1) >> level) & mask;

            Node<ListItem<E>> tmp;
            Node<ListItem<E>> newNode;
            if (currentNode.getChild() == null) {
                currentNode.setChild(new LinkedList<>());
                newNode = new Node<>();
                currentNode.getChild().add(newNode);
            } else {
                if (widthIndex == currentNode.getChild().size()) {
                    newNode = new Node<>();
                    currentNode.getChild().add(newNode);
                } else {
                    tmp = currentNode.getChild().get(widthIndex);
                    newNode = new Node<>(tmp);
                    currentNode.getChild().set(widthIndex, newNode);
                }
            }
            currentNode = newNode;
        }

        if (currentNode.getValue() == null) {
            currentNode.setValue(new ArrayList<>());
        }

        return currentNode;
    }

    public Pair<Node<ListItem<E>>, Integer> copyLeaf(ListHead<ListItem<E>> newHead, int index) {
        Node<ListItem<E>> currentNode = newHead.getRoot();
        for (int level = bits * (depth - 1); level > 0; level -= bits) {
            int widthIndex = (index >> level) & mask;
            Node<ListItem<E>> tmp;
            Node<ListItem<E>> newNode;
            tmp = currentNode.getChild().get(widthIndex);
            newNode = new Node<>(tmp);
            currentNode.getChild().set(widthIndex, newNode);
            currentNode = newNode;
        }

        return new Pair<>(currentNode, index & mask);
    }

    public Pair<Node<ListItem<E>>, Integer> getLeaf(ListHead<ListItem<E>> head, int index) {
        Node<ListItem<E>> node = head.getRoot();
        for (int level = bits * (depth - 1); level > 0; level -= bits) {
            int widthIndex = (index >> level) & mask;
            node = node.getChild().get(widthIndex);
        }

        return new Pair<>(node, index & mask);
    }

    public void remove(int index){
        findNode(index).getValue().remove(index & mask);

        size--;
    }

    public Node<E> findNode(int index) {
        Node<E> foundNode = root;

        for (int level = bits * (depth - 1); level > 0; level -= bits) {
            int widthIndex = (index >> level) & mask;
            int widthIndexNext = (index >> (level - bits)) & mask;

            Node<E> childNode = foundNode.getChild().get(widthIndex);
            Node<E> newNode = new Node<>(childNode, widthIndexNext);
            foundNode.getChild().set(widthIndex, newNode);
            foundNode = newNode;
        }

        return foundNode;
    }

    public Node<E> createSubTree(int maxIndex) {
        Node<E> myNode = new Node<>(root, ((maxIndex - 1 >> (bits * (depth - 1))) & mask));
        Node<E> foundNode = myNode;

        for (int level = bits * (depth - 1); level > 0; level -= bits) {
            int widthIndex = ((maxIndex - 1) >> level) & mask;
            int widthIndexNext = ((maxIndex - 1) >> (level - bits)) & mask;

            Node<E> childNode = foundNode.getChild().get(widthIndex);
            Node<E> newNode = new Node<>(childNode, widthIndexNext);
            foundNode.getChild().set(widthIndex, newNode);
            foundNode = newNode;
        }

        return myNode;
    }

    public E get(int index) {
        if(index < 0 || index >= size){
            throw new IndexOutOfBoundsException();
        }

        Node<E> foundNode = root;

        for (int level = bits * (depth - 1); level > 0; level -= bits) {
            int widthIndex = (index >> level) & mask;
            foundNode = foundNode.getChild().get(widthIndex);
        }

        return foundNode.getValue().get(index & mask);
    }
}