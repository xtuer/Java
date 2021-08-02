package algorithm;

import java.util.Stack;

public class BTree {
    static class Node {
        int  data;
        Node left;
        Node right;

        public Node(int data) {
            this.data = data;
        }
    }

    private Node root;

    public BTree(int... ns) {
        root = build(ns);
    }

    public static Node build(int... ns) {
        Node[] nodes = new Node[ns.length];

        for (int i = 0; i < ns.length; i++) {
            nodes[i] = new Node(ns[i]);
        }

        for (int i = 0; i < nodes.length; i++) {
            int left  = i * 2 + 1;
            int right = i * 2 + 2;

            if (left < nodes.length) {
                nodes[i].left = nodes[left];
            }

            if (right < nodes.length) {
                nodes[i].right = nodes[right];
            }
        }

        return nodes[0];
    }

    public void preOrderTraverse() {
        preOrderTraverse(root);
    }

    private void preOrderTraverse(Node node) {
        if (node == null) { return; }

        visit(node);
        preOrderTraverse(node.left);
        preOrderTraverse(node.right);
    }

    public void preOrderTraverse2() {
        Stack<Node> stack = new Stack<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            Node node = stack.pop();
            visit(node);

            if (node.right != null) {
                stack.push(node.right);
            }

            if (node.left != null) {
                stack.push(node.left);
            }
        }
    }

    public void midOrderTraverse() {
        midOrderTraverse(root);
    }

    public void midOrderTraverse(Node node) {
        if (node == null) { return; }

        midOrderTraverse(node.left);
        visit(node);
        midOrderTraverse(node.right);
    }

    public void midOrderTraverse2() {
        Stack<Node> stack = new Stack<>();
        Node node = root;

        while (node != null || !stack.isEmpty()) {
            while (node != null) {
                stack.push(node);
                node = node.left;
            }

            node = stack.pop();
            visit(node);
            node = node.right;
        }
    }

    public void postOrderTraverse() {
        postOrderTraverse(root);
    }

    public void postOrderTraverse(Node node) {
        if (node == null) { return; }

        postOrderTraverse(node.left);
        postOrderTraverse(node.right);
        visit(node);
    }

    public void visit(Node node) {
        System.out.print(node.data + ", ");
    }

    public static void main(String[] args) {
        //      0
        //   1     2
        // 3  4  5   6
        BTree tree = new BTree(0, 1, 2, 3, 4, 5, 6);

        tree.preOrderTraverse();
        System.out.println();
        tree.preOrderTraverse2();
        System.out.println();

        tree.midOrderTraverse();
        System.out.println();
        tree.midOrderTraverse2();
        System.out.println();

        tree.postOrderTraverse();
        System.out.println();
    }
}
