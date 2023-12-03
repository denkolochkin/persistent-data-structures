package ru.nsu.fit.util.tree;

import ru.nsu.fit.util.node.Node;

import java.util.ArrayList;
import java.util.LinkedList;

public class BTree<E> {
    private int DEPTH;
    private int MASK;
    private int MAXSIZE;
    private int BITS = 2;
    private int WIDTH;
    private final Node<E> root;
    private int size = 0;

    public BTree() {
        initialization(1, 4);
        this.root = new Node<>();
    }

    public BTree(int size) {
        initialization((int) Math.ceil(Math.log(size) / Math.log( (int) Math.pow(2, BITS))), BITS);
        this.root = new Node<>();
    }

    public BTree(BTree<E> other) {
        initialization(other.DEPTH,other.BITS);
        this.root = new Node<>(other.root);
        this.size = other.size;
    }

    public BTree(BTree<E> other, Integer newSize, Integer maxIndex) {
        initialization(other.DEPTH,other.BITS);
        this.root = new Node<>(other.root, maxIndex);
        this.size = newSize;
    }

    protected void initialization(int depth, int bits) {
        if(depth > 0) {
            this.DEPTH = depth;
            this.BITS = bits;
        }else{
            this.DEPTH = 1;
            this.BITS = bits;
        }

        updateInformationAboutTree();
    }

    private void updateInformationAboutTree(){
        MASK = (int) Math.pow(2, BITS) - 1;
        MAXSIZE = (int) Math.pow(2, BITS * DEPTH);
        WIDTH = (int) Math.pow(2, BITS);
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
        DEPTH++;

        updateInformationAboutTree();
    }

    public boolean add(E element) {
        size++;

        if(size > MAXSIZE){
            increaseDepthOfTree();
        }

        Node<E> foundNode = root;

        for (int level = BITS * (DEPTH - 1); level > 0; level -= BITS) {
            int widthIndex = ((size - 1) >> level) & MASK;
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
        findNode(index).getValue().set(index & MASK, element);

        return true;
    }

    public void set(int index, E element) {
        Node<E> foundNode = root;

        for (int level = BITS * (DEPTH - 1); level > 0; level -= BITS) {
            int widthIndex = (index >> level) & MASK;
            Node<E> childNode = foundNode.getChild().get(widthIndex);
            Node<E> newNode = new Node<>(childNode);
            foundNode.getChild().set(widthIndex, newNode);
            foundNode = newNode;
        }

        foundNode.getValue().set(index & MASK, element);
    }

    public void remove(int index){
        findNode(index).getValue().remove(index & MASK);

        size--;
    }

    public Node<E> findNode(int index) {
        Node<E> foundNode = root;

        for (int level = BITS * (DEPTH - 1); level > 0; level -= BITS) {
            int widthIndex = (index >> level) & MASK;
            int widthIndexNext = (index >> (level - BITS)) & MASK;

            Node<E> childNode = foundNode.getChild().get(widthIndex);
            Node<E> newNode = new Node<>(childNode, widthIndexNext);
            foundNode.getChild().set(widthIndex, newNode);
            foundNode = newNode;
        }

        return foundNode;
    }

    public E get(int index) {
        if(index < 0 || index >= size){
            throw new IndexOutOfBoundsException();
        }

        Node<E> foundNode = root;

        for (int level = BITS * (DEPTH - 1); level > 0; level -= BITS) {
            int widthIndex = (index >> level) & MASK;
            foundNode = foundNode.getChild().get(widthIndex);
        }

        return foundNode.getValue().get(index & MASK);
    }

    public int getMaxIndex(int index){
        return (index >> (BITS * (DEPTH - 1))) & MASK;
    }
}