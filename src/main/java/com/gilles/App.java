package com.gilles;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
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
import com.gilles.UI.BA900TablePane;
import com.gilles.Util.CSVWriter;

/**
 * Hello world!
 *
 */
public class App {

    private static JTabbedPane tabPane;

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
        ArrayList<Integer> years = new ArrayList<>();
        for (int i = 1990; i < 2025; i++) {
            years.add(i);
        }
        JComboBox year = new JComboBox<>(years.toArray());
        ArrayList<Integer> months = new ArrayList<>();
        for (int i = 1; i < 13; i++) {
            months.add(i);
        }
        JComboBox month = new JComboBox<>(months.toArray());
        year.setPreferredSize(new Dimension(100, 25));
        month.setPreferredSize(new Dimension(100, 25));
        JButton b = new JButton("Go");
        JTabbedPane tabs = new JTabbedPane();
        tabPane = tabs;
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

    private static void selectionButtonPressed(JComboBox y, JComboBox m, JComboBox c, DataStore d, JFrame frame,
            JTabbedPane tabs) throws IOException {
        System.gc();
        tabs.removeAll();
        HashMap<String, HashMap<YearMonth, BA900Record>> map = d.getAllRecords();
        HashMap<YearMonth, BA900Record> bank = map.get(c.getSelectedItem().toString());
        BA900Record record = bank
                .get(YearMonth.of((Integer) (y.getSelectedItem()), (Integer) (m.getSelectedItem())));

        frame.add(tabs);
        for (BA900Table tab : record.getTables()) {
            JTable jt = new JTable(tab.getRecordsAs2dArray(), tab.getColumns());
            jt.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {

                    int row = jt.getSelectedRow();
                    int col = jt.getSelectedColumn();
                    String columnName = tab.getColumns()[col];
                    String desc = jt.getValueAt(row, 0).toString();
                    System.out.println(tab.getValueBasedOnDescAndCol(desc, columnName));
                    HashMap<String, HashMap<YearMonth, BA900Record>> innerMap = d.getAllRecords();
                    HashMap<YearMonth, BA900Record> innerBank = innerMap.get(c.getSelectedItem().toString());

                    String[] csvCols = { "yearMonth", columnName, "Description in table", "Column in table" };
                    String append = "";
                    for (String name : innerMap.keySet()) {
                        HashMap<YearMonth, BA900Record> aBank = innerMap.get(name);
                        CSVWriter w = new CSVWriter(csvCols);
                        for (BA900Record recordFromMassPrint : aBank.values()) {

                            try {
                                for (BA900Table tableInBank : recordFromMassPrint.getTables()) {
                                    if (tableInBank.getTableName().equals(tab.getTableName())) {
                                        // System.out.println(tableInBank.getValueBasedOnDescAndCol(desc, columnName));
                                        // System.out.println(tableInBank.getValueBasedOnIndexLikeANormalPerson(row,
                                        // col));
                                        String[] toAdd = { recordFromMassPrint.getRecordDate().toString(),
                                                tableInBank.getValueBasedOnIndexLikeANormalPerson(row, col),
                                                tableInBank.getCol0ofRow(row), tableInBank.getColumnName(col) };
                                        w.addRecord(toAdd);
                                    }
                                }
                            } catch (IOException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                        }
                        try {
                            w.write(name + "/" + name + "_" + desc + "_" + columnName);
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                }
            });
            BA900TablePane sp = new BA900TablePane(jt, jt);
            tabs.add(tab.getTableName(), sp);

        }
    }

}
