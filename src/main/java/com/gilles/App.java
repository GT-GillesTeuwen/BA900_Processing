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
import javax.swing.table.DefaultTableModel;
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
    static ArrayList<String> tableNamesToFind;
    static ArrayList<Integer> rows;
    static ArrayList<Integer> cols;
    static ArrayList<String> descriptions;
    static ArrayList<String> columnNames;

    public static void main(String[] args) {
        tableNamesToFind = new ArrayList<>();
        rows = new ArrayList<>();
        cols = new ArrayList<>();
        descriptions = new ArrayList<>();
        columnNames = new ArrayList<>();
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
        String[] cols = { "Table Name", "Row", "Col", "RowDesc", "ColName" };
        JTable selectedCells = new JTable(new DefaultTableModel(cols, 0));
        JButton clearBtn = new JButton("Clear");
        JTextField csvName = new JTextField("");
        csvName.setPreferredSize(new Dimension(100, 25));
        JButton buildCSV = new JButton("Build CSV");
        tabPane = tabs;
        p.add(comboBox);
        p.add(year);
        p.add(month);
        p.add(b);
        p.add(new JScrollPane(selectedCells));
        p.add(clearBtn);
        p.add(csvName);
        p.add(buildCSV);
        cont.add(p);
        cont.setSize(300, 400);
        cont.setVisible(true);
        JFrame frame = new JFrame("Data");
        frame.setSize(300, 400);
        frame.setVisible(true);
        b.addActionListener(e -> {
            try {
                selectionButtonPressed(year, month, comboBox, d, frame, tabs, selectedCells);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        clearBtn.addActionListener(e -> {
            clearSelectedCells(selectedCells);
        });
        buildCSV.addActionListener(e -> {
            try {
                doSCV(csvName.getText(), d);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void clearSelectedCells(JTable selectedCells) {
        DefaultTableModel dm = (DefaultTableModel) selectedCells.getModel();
        int rowCount = dm.getRowCount();
        // Remove rows one by one from the end of the table
        for (int i = rowCount - 1; i >= 0; i--) {
            dm.removeRow(i);
        }
        tableNamesToFind.clear();
        rows.clear();
        cols.clear();
        descriptions.clear();
        columnNames.clear();
    }

    private static void selectionButtonPressed(JComboBox y, JComboBox m, JComboBox c, DataStore d, JFrame frame,
            JTabbedPane tabs, JTable selectedCells) throws IOException {
        System.gc();
        tabs.removeAll();
        HashMap<String, HashMap<YearMonth, BA900Record>> map = d.getAllRecords();
        HashMap<YearMonth, BA900Record> bank = map.get(c.getSelectedItem().toString());
        BA900Record record = bank
                .get(YearMonth.of((Integer) (y.getSelectedItem()), (Integer) (m.getSelectedItem())));

        frame.add(tabs);
        for (BA900Table tab : record.getTables().values()) {
            JTable jt = new JTable(tab.getRecordsAs2dArray(), tab.getColumns());
            jt.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {

                    int row = jt.getSelectedRow();
                    int col = jt.getSelectedColumn();
                    String columnName = tab.getColumns()[col];
                    String desc = jt.getValueAt(row, 0).toString();
                    System.out.println(tab.getValueBasedOnDescAndCol(desc, columnName));

                    tableNamesToFind.add(tab.getTableName());
                    rows.add(row);
                    cols.add(col);
                    descriptions.add(desc);
                    columnNames.add(columnName);

                    DefaultTableModel model = (DefaultTableModel) selectedCells.getModel();
                    model.addRow(new Object[] { tab.getTableName(), row, col, desc, columnName });

                }
            });
            BA900TablePane sp = new BA900TablePane(jt, jt);
            tabs.add(tab.getTableName(), sp);

        }
    }

    private static void doSCV(String tableName, DataStore d) throws IOException {
        for (String bankName : d.getAllRecords().keySet()) {
            ArrayList<String> csvColumns = new ArrayList<>();
            csvColumns.add("year-month");
            for (int i = 0; i < cols.size(); i++) {
                csvColumns.add(descriptions.get(i) + "-" + columnNames.get(i));
            }
            CSVWriter writer = new CSVWriter(csvColumns);
            HashMap<YearMonth, BA900Record> currentBank = d.getAllRecords().get(bankName);
            for (YearMonth yearMonth : currentBank.keySet()) {
                BA900Record currentRecord = currentBank.get(yearMonth);
                String[] newRecordForCSV = new String[csvColumns.size()];
                newRecordForCSV[0] = yearMonth.toString();
                for (int i = 0; i < tableNamesToFind.size(); i++) {
                    BA900Table currentTable = currentRecord.getTables().get(tableNamesToFind.get(i));
                    int currentRow = rows.get(i);
                    int currentCol = cols.get(i);
                    if (currentTable == null) {
                        newRecordForCSV[i + 1] = "NOVALUEFOUND";
                    } else {
                        newRecordForCSV[i + 1] = currentTable.getValueBasedOnIndexLikeANormalPerson(currentRow,
                                currentCol);
                    }
                }
                writer.addRecord(newRecordForCSV);
            }
            writer.write(bankName + "/" + bankName + "-" + tableName);
        }
    }

}
