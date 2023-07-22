package com.gilles;

import java.awt.Dimension;
import java.io.IOException;
import java.time.YearMonth;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.xml.crypto.Data;

import com.gilles.Core.DataStore;
import com.gilles.DataClasses.BA900Record;
import com.gilles.DataClasses.BA900Table;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        JFrame ld = new JFrame("Loading");
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        ld.add(progressBar);
        ld.pack();
        ld.setVisible(true);

        DataStore d = new DataStore("Data");
        ld.dispose();
        System.gc();
        JComboBox comboBox = new JComboBox<>(d.getAllRecords().keySet().toArray());
        comboBox.setPreferredSize(new Dimension(350, 25));

        JFrame cont = new JFrame("Cont");
        cont.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel p = new JPanel();
        JTextField year = new JTextField();
        JTextField month = new JTextField();
        year.setPreferredSize(new Dimension(100, 25));
        month.setPreferredSize(new Dimension(100, 25));
        JButton b = new JButton("Go");
        JTabbedPane tabs = new JTabbedPane();
        p.add(comboBox);
        p.add(year);
        p.add(month);
        p.add(b);
        cont.add(p);
        cont.setSize(300, 400);
        cont.setVisible(true);
        JFrame frame = new JFrame("Data");
        frame.setSize(300, 400);
        frame.setVisible(true);
        b.addActionListener(e -> {
            try {
                selectionButtonPressed(year, month, comboBox, d, frame, tabs);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void selectionButtonPressed(JTextField y, JTextField m, JComboBox c, DataStore d, JFrame frame,
            JTabbedPane tabs) throws IOException {
        System.gc();
        tabs.removeAll();
        HashMap<String, HashMap<YearMonth, BA900Record>> map = d.getAllRecords();
        HashMap<YearMonth, BA900Record> bank = map.get(c.getSelectedItem().toString());
        BA900Record record = bank.get(YearMonth.of(Integer.parseInt(y.getText()), Integer.parseInt(m.getText())));

        frame.add(tabs);
        for (BA900Table tab : record.getTables()) {
            JTable jt = new JTable(tab.getRecordsAs2dArray(), tab.getColumns());
            JScrollPane sp = new JScrollPane(jt);
            tabs.add(tab.getTableName(), sp);

        }
    }
}
