package it.unicam.pros.pplg.util;

public class BinaryTree<T> {

    private BinaryNode root;

    public BinaryTree(){
        root = null;
    }

    public BinaryNode getRoot() {
        return root;
    }

    public void setRoot(BinaryNode node) {
        this.root = node;
    }



}
