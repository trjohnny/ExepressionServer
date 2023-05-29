package com.project.domain.expression;

import com.project.domain.expression.nodes.Node;

public class Expression {
    private final Node root;
    private final String expressionString;

    public Expression(Node root, String expressionString) {
        this.root = root;
        this.expressionString = expressionString;
    }

    public Node getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return expressionString;
    }
}

