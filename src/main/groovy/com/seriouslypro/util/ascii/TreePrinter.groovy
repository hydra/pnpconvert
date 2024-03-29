package com.seriouslypro.util.ascii

class TreePrinter<T> {
    private final String CROSS = " ├─";
    private final String CORNER = " └─";
    private final String VERTICAL = " │ ";
    private final String SPACE = "   ";

    List<String> lines
    String line

    List<String> print(TreeNode<T> node) {
        lines = []
        line = ""
        printNode(node, "")

        lines
    }

    void printNode(TreeNode<T> node, String indent) {
        line += node.value.toString()
        lines << line
        line = ""

        var childrenCount = node.children.size()
        node.children.eachWithIndex { TreeNode<T> child, int i ->
            boolean isLast = i == (childrenCount - 1)
            printChild(child, indent, isLast)
        }
    }

    void printChild(TreeNode<T> node, String indent, boolean isLast) {
        line += indent
        if (isLast) {
            line += CORNER
            indent += SPACE
        } else {
            line += CROSS
            indent += VERTICAL
        }
        printNode(node, indent)
    }
}
