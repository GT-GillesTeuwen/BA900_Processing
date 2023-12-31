package com.gilles;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.xml.crypto.Data;

import org.json.JSONArray;
import org.json.JSONObject;

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
    static JTable selectedCells;
    static JTextArea pasteArea;

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
        // comboBox.setPreferredSize(new Dimension(350, 25));

        JFrame cont = new JFrame("Cont");
        cont.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel p = new JPanel(new GridBagLayout());
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
        year.setMaximumSize(new Dimension(100, 25));
        // month.setPreferredSize(new Dimension(100, 25));
        JButton b = new JButton("Go");
        JTabbedPane tabs = new JTabbedPane();
        String[] cols = { "Table Name", "Row", "Col", "RowDesc", "ColName" };
        selectedCells = new JTable(new DefaultTableModel(cols, 0));
        JButton clearBtn = new JButton("Clear");
        JTextField csvName = new JTextField("");
        csvName.setPreferredSize(new Dimension(100, 25));
        JCheckBox useRawColIntAndRawRowInt = new JCheckBox("Raw Indexes");
        JButton buildCSV = new JButton("Build CSV");
        JButton buildCSVFromText = new JButton("Build from text");
        tabPane = tabs;
        pasteArea = new JTextArea("paste here");
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        p.add(comboBox, c);
        c.gridwidth = 1;

        c.gridx = 0;
        c.gridy = 1;
        p.add(year, c);

        c.gridx = 1;
        c.gridy = 1;
        p.add(month, c);

        c.gridx = 2;
        c.gridy = 1;
        p.add(b, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 3;
        p.add(new JScrollPane(selectedCells), c);

        c.gridx = 0;
        c.gridy = 3;
        p.add(clearBtn, c);
        c.gridwidth = 1;

        c.gridx = 0;
        c.gridy = 4;
        p.add(csvName, c);

        c.gridx = 1;
        c.gridy = 4;
        p.add(useRawColIntAndRawRowInt, c);

        c.gridx = 2;
        c.gridy = 4;
        p.add(buildCSV, c);

        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 3;
        p.add(new JLabel("Paste pre-built searches here"), c);
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 3;
        JScrollPane sp = new JScrollPane(pasteArea);
        sp.setPreferredSize(new Dimension(100, 100));
        p.add(sp, c);

        c.gridx = 0;
        c.gridy = 7;
        p.add(buildCSVFromText, c);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                p, tabPane);
        cont.add(splitPane);
        cont.setVisible(true);
        b.addActionListener(e -> {
            try {
                selectionButtonPressed(year, month, comboBox, d, tabs, selectedCells);
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

                doSCV(csvName.getText(), d, selectedCells, useRawColIntAndRawRowInt);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        buildCSVFromText.addActionListener(e -> {
            try {
                addFromTextArea();

                doSCVFromPasted(csvName.getText(), d, useRawColIntAndRawRowInt);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
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

    public static JSONObject getConfig() {
        Scanner file;
        String json = "";
        try {
            file = new Scanner(new FileReader("config/renameCols.conf"));

            while (file.hasNext()) {
                json += file.nextLine();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("no config found");
            return new JSONObject("{}");
        }

        return new JSONObject(json);
    }

    private static void addFromTextArea() {
        clearSelectedCells(selectedCells);
        String prebuiltSearch = pasteArea.getText();
        String[] lines = prebuiltSearch.split("\n");
        for (String string : lines) {
            String[] split = string.split(";");
            tableNamesToFind.add(split[0]);
            rows.add(Integer.parseInt(split[1]));
            cols.add(Integer.parseInt(split[2]));
            descriptions.add(split[3]);
            columnNames.add(split[4]);
        }
    }

    private static void selectionButtonPressed(JComboBox y, JComboBox m, JComboBox c, DataStore d,
            JTabbedPane tabs, JTable selectedCells) throws IOException {
        System.gc();
        tabs.removeAll();
        HashMap<String, HashMap<YearMonth, BA900Record>> map = d.getAllRecords();
        HashMap<YearMonth, BA900Record> bank = map.get(c.getSelectedItem().toString());
        BA900Record record = bank
                .get(YearMonth.of((Integer) (y.getSelectedItem()), (Integer) (m.getSelectedItem())));

        for (BA900Table tab : record.getTables(getConfig()).values()) {
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

    private static void doSCVFromPasted(String tableName, DataStore d, JCheckBox rawCol)
            throws IOException {
        System.out.println("FROM pasted");
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors
                .newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
        JSONObject conf = getConfig();
        HashSet<String> banks = new HashSet<>();
        JSONArray bankJson = conf.getJSONArray("Banks");
        for (int i = 0; i < bankJson.length(); i++) {
            banks.add(bankJson.getString(i));
        }
        for (String bankName : d.getAllRecords().keySet()) {
            if (banks.contains(bankName)) {
                System.out.println("DOING " + bankName);
                executor.submit(() -> {
                    ArrayList<String> csvColumns = new ArrayList<>();
                    csvColumns.add("year-month");
                    for (int i = 0; i < cols.size(); i++) {
                        csvColumns.add(descriptions.get(i) + "-" + columnNames.get(i));
                        csvColumns.add("rawColheading_" + i);
                    }
                    CSVWriter writer = new CSVWriter(csvColumns);
                    HashMap<YearMonth, BA900Record> currentBank = d.getAllRecords().get(bankName);
                    for (YearMonth yearMonth : currentBank.keySet()) {
                        if (yearMonth == null) {
                            break;
                        }

                        BA900Record currentRecord = currentBank.get(yearMonth);
                        String[] newRecordForCSV = new String[csvColumns.size()];
                        newRecordForCSV[0] = yearMonth.toString();
                        for (int i = 0; i < tableNamesToFind.size(); i++) {
                            BA900Table currentTable = null;
                            try {
                                currentTable = currentRecord.getTables(getConfig()).get(tableNamesToFind.get(i));
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            int currentRow = rows.get(i);
                            int currentCol = cols.get(i);
                            String rowSubString = descriptions.get(i);
                            String colSubString = columnNames.get(i);
                            // System.out.println(subString);
                            if (currentTable == null) {
                                newRecordForCSV[(2 * (i + 1)) - 1] = "NO VALUE FOUND, Table did not exist";
                                newRecordForCSV[2 * (i + 1)] = "NO VALUE FOUND, Table did not exist";
                            } else {
                                if (rawCol.isSelected()) {
                                    String res = currentTable.getValueBasedOnIndexLikeANormalPerson(
                                            currentRow,
                                            currentCol);
                                    newRecordForCSV[(2 * (i + 1)) - 1] = res;
                                } else {
                                    String res = currentTable
                                            .getValueBasedOnDescriptionContainsAndColumnContains(
                                                    rowSubString,
                                                    colSubString);
                                    newRecordForCSV[(2 * (i + 1)) - 1] = res.split("~")[0];
                                    newRecordForCSV[(2 * (i + 1))] = res.split("~")[1];
                                }

                            }
                        }
                        writer.addRecord(newRecordForCSV);
                    }
                    try {
                        writer.write(bankName + "/" + bankName + "-" + tableName);
                    } catch (IOException e) {
                        System.out.println("a");
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private static void doSCV(String tableName, DataStore d, JTable selectedCells, JCheckBox rawCol)
            throws IOException {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors
                .newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
        JSONObject conf = getConfig();
        HashSet<String> banks = new HashSet<>();
        JSONArray bankJson = conf.getJSONArray("Banks");
        for (int i = 0; i < bankJson.length(); i++) {
            banks.add(bankJson.getString(i));
        }
        for (String bankName : d.getAllRecords().keySet()) {
            if (banks.contains(bankName)) {
                System.out.println("DOING " + bankName);
                executor.submit(() -> {
                    ArrayList<String> csvColumns = new ArrayList<>();
                    csvColumns.add("year-month");
                    for (int i = 0; i < cols.size(); i++) {
                        csvColumns.add(descriptions.get(i) + "-" + columnNames.get(i));
                        csvColumns.add("rawColheading_" + i);
                    }
                    CSVWriter writer = new CSVWriter(csvColumns);
                    HashMap<YearMonth, BA900Record> currentBank = d.getAllRecords().get(bankName);
                    for (YearMonth yearMonth : currentBank.keySet()) {
                        if (yearMonth == null) {
                            break;
                        }

                        BA900Record currentRecord = currentBank.get(yearMonth);
                        String[] newRecordForCSV = new String[csvColumns.size()];
                        newRecordForCSV[0] = yearMonth.toString();
                        for (int i = 0; i < tableNamesToFind.size(); i++) {
                            BA900Table currentTable = null;
                            try {
                                currentTable = currentRecord.getTables(getConfig()).get(tableNamesToFind.get(i));
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            int currentRow = rows.get(i);
                            int currentCol = cols.get(i);
                            String rowSubString = selectedCells.getValueAt(i, 3).toString();
                            String colSubString = selectedCells.getValueAt(i, 4).toString();
                            // System.out.println(subString);
                            if (currentTable == null) {
                                newRecordForCSV[(2 * (i + 1)) - 1] = "NO VALUE FOUND, Table did not exist";
                                newRecordForCSV[2 * (i + 1)] = "NO VALUE FOUND, Table did not exist";
                            } else {
                                if (rawCol.isSelected()) {
                                    String res = currentTable.getValueBasedOnIndexLikeANormalPerson(
                                            currentRow,
                                            currentCol);
                                    newRecordForCSV[(2 * (i + 1)) - 1] = res;
                                } else {
                                    String res = currentTable
                                            .getValueBasedOnDescriptionContainsAndColumnContains(
                                                    rowSubString,
                                                    colSubString);
                                    newRecordForCSV[(2 * (i + 1)) - 1] = res.split("~")[0];
                                    newRecordForCSV[(2 * (i + 1))] = res.split("~")[1];
                                }

                            }
                        }
                        writer.addRecord(newRecordForCSV);
                    }
                    try {
                        writer.write(bankName + "/" + bankName + "-" + tableName);
                    } catch (IOException e) {
                        System.out.println("a");
                        e.printStackTrace();
                    }
                });
            }
        }
    }

}
