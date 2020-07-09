package it.unicam.pros.pplg.util;

public class BinaryNode<T> {

    private T element;
    private BinaryNode right, left, parent;

    public BinaryNode(T element, BinaryNode parent){
        this.element = element;
        this.parent = parent;
        this.right = null;
        this.left = null;
    }

    public T getElement() {
        return element;
    }

    public void setElement(T element) {
        this.element = element;
    }
    public BinaryNode getLeft() {
        return left;
    }

    public BinaryNode getRight() {
        return right;
    }


    public void setLeft(BinaryNode left) {
        this.left = left;
    }

    public void setRight(BinaryNode right) {
        this.right = right;
    }

}
