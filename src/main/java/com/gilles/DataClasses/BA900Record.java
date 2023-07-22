package com.gilles.DataClasses;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.time.YearMonth;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.gilles.Exceptions.InvalidRecordException;

public class BA900Record {
    private YearMonth recordDate;
    private String institution;
    private String formType;
    private String path;

    public BA900Record(YearMonth date, String institution, String formType, String path) {
        this.recordDate = date;
        this.institution = institution;
        this.formType = formType;
        this.path = path;
    }

    // public void addTable(String tableName, String[] columns) {
    // BA900Table currentTable = new BA900Table(tableName, columns);
    // tables.put(tableName, currentTable);
    // }

    public String toString() {
        return "Institution:" + institution + "\tDate:" + recordDate.toString();
    }

    public Set<BA900Table> getTables() throws IOException {
        Set<BA900Table> allTables = new HashSet<>();
        Scanner file = new Scanner(new FileReader(path));
        String cur = "";
        while (file.hasNext()) {
            while (file.hasNext() && !cur.startsWith("Table")) {
                cur = file.nextLine();
            }
            String tableName = cur;
            String headings = file.nextLine();
            if (headings.charAt(headings.length() - 1) == ',') {
                headings = headings.substring(0, headings.length() - 1);
            }
            String[] columns = headings.split(",");

            cur = file.nextLine();
            if (cur.charAt(cur.length() - 1) == ',') {
                cur = cur.substring(0, cur.length() - 1);
            }
            BA900Table currentTable = new BA900Table(tableName, columns);
            allTables.add(currentTable);
            while (file.hasNext() && !cur.startsWith("Table")) {
                Reader in = new StringReader(cur);
                for (CSVRecord row : CSVFormat.DEFAULT.parse(in)) {
                    try {
                        currentTable.addRecord(row);
                    } catch (InvalidRecordException e) {
                        // TODO Auto-generated catch block
                        System.out.println(e.getMessage() + " in " + path);
                        System.exit(1);
                    }
                }
                in.close();
                cur = file.nextLine();
                if (cur.charAt(cur.length() - 1) == ',') {
                    cur = cur.substring(0, cur.length() - 1);
                }
            }
        }
        file.close();
        return allTables;
    }
}
