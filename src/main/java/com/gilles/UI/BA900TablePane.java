package com.gilles.UI;

import java.awt.Component;
import java.awt.ScrollPane;

import javax.swing.JScrollPane;
import javax.swing.JTable;

public class BA900TablePane extends JScrollPane {
    private JTable table;

    public BA900TablePane(Component view, JTable table) {
        super(view);
        this.table = table;
    }

    public JTable getTable() {
        return table;
    }

}
