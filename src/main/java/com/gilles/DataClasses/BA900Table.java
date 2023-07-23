package com.gilles.DataClasses;

import java.util.ArrayList;

import org.apache.commons.csv.CSVRecord;

import com.gilles.Exceptions.InvalidRecordException;

public class BA900Table {
    private String tableName;
    private BA900Record record;
    private String[] columns;
    private ArrayList<String[]> records;

    public BA900Table(String tableName, BA900Record record, String... cols) {
        this.record = record;
        this.tableName = tableName;
        columns = new String[cols.length];
        for (int i = 0; i < cols.length; i++) {
            columns[i] = cols[i].replace("\"", "").toLowerCase();
        }
        records = new ArrayList<>();

    }

    public String getValueBasedOnDescAndCol(String desc, String col) {
        int columnIndex = -1;
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(col)) {
                columnIndex = i;
                break;
            }
        }

        int rowIndex = -1;
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i)[0].equals(desc)) {
                rowIndex = i;
                break;
            }
        }
        if (columnIndex == -1 || rowIndex == -1) {
            return "Value not found in " + record.getRecordDate() + " " + record.getPath() + "\n" + "Either " + desc
                    + " or " + col + " is not in this table";
        }

        return records.get(rowIndex)[columnIndex];
    }

    public String getCol0ofRow(int row) {
        return records.get(row)[0];
    }

    public String getColumnName(int col) {
        return columns[col];
    }

    // This assumes that all tablesN's have the same number of rows and cols
    public String getValueBasedOnIndexLikeANormalPerson(int row, int col) {
        return records.get(row)[col];
    }

    public String getTableName() {
        return tableName;
    }

    public void addRecord(CSVRecord record) throws InvalidRecordException {
        // if (record.size() != columns.length) {
        // String err = "Cannot add record with " + record.size() + " columns to " +
        // tableName
        // + " because " + tableName + " has " + columns.length + " columns\n" + "Data
        // was:";
        // for (int i = 0; i < record.size(); i++) {
        // err += "\n" + (i + 1) + ". " + record.get(i);
        // }
        // throw new InvalidRecordException(err + "\n");
        // }
        String[] recordArr = new String[record.size()];
        for (int i = 0; i < record.size(); i++) {
            recordArr[i] = record.get(i);
        }
        records.add(recordArr);
    }

    public String[] getColumns() {
        return columns;
    }

    public ArrayList<String[]> getRecords() {
        return records;
    }

    public String[][] getRecordsAs2dArray() {
        String[][] rows = new String[records
                .size()][columns.length];
        int row = 0;
        int col = 0;
        for (String[] strings : records) {
            for (String string : strings) {
                rows[row][col] = string;
                col++;
            }
            row++;
            col = 0;
        }
        return rows;
    }

    public String getTableString() {
        String out = "";
        for (String string : columns) {
            out += string + "|";
        }
        System.out.println("\n");
        for (String[] string : records) {
            for (String string2 : string) {
                out += string2 + "|";
            }
            System.out.println();
        }
        return out;
    }
}
