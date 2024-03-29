package com.seriouslypro.util.ascii

class TreeNode<T> {
    T value
    List<TreeNode<T>> children = []

    TreeNode(T value) {
        this.value = value
    }

    void addChild(TreeNode child) {
        this.children << child
    }
}
