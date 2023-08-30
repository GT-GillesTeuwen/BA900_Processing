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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public String getPath() {
        return path;
    }

    public YearMonth getRecordDate() {
        return recordDate;
    }

    public HashMap<String, BA900Table> getTables(JSONObject replacements) throws IOException {
        HashMap<String, BA900Table> allTables = new HashMap<>();
        Scanner file = new Scanner(new FileReader(path));
        String currentLineOfTheFile = "";
        while (file.hasNext()) {

            // Find the next table
            while (file.hasNext() && !currentLineOfTheFile.startsWith("Table")) {
                currentLineOfTheFile = file.nextLine();
            }
            String tableName = currentLineOfTheFile;
            tableName = tableName.replaceAll(",", "");
            String headings = file.nextLine();

            // Get the headings of the table
            if (headings.charAt(headings.length() - 1) == ',') {
                headings = headings.substring(0, headings.length() - 1);
            }

            // Building the columns array
            String[] columns = headings.split(",");

            for (int i = 0; i < columns.length; i++) {
                columns[i] = columns[i].toLowerCase();
                columns[i] = columns[i].replaceAll("\"", "");
            }
            // Replace column names based on passed in map
            // YearMonth-->Table-->Col-->newValue
            try {
                for (int i = 0; i < replacements.getJSONArray("YearMonths").length(); i++) {
                    JSONObject yearMonth = replacements.getJSONArray("YearMonths").getJSONObject(i);
                    if (yearMonth.getString("YearMonth").equals(recordDate.toString())) {
                        // System.out.println("FOUND YEAR");
                        for (int j = 0; j < yearMonth.getJSONArray("tables").length(); j++) {
                            JSONObject table = yearMonth.getJSONArray("tables").getJSONObject(j);
                            if (table.getString("table").equals(tableName)) {
                                // System.out.println("\tFOUND TABLE");
                                for (int k = 0; k < table.getJSONArray("columns").length(); k++) {

                                    for (int l = 0; l < columns.length; l++) {
                                        // System.out.println(columns[l] + " ?= "
                                        // + table.getJSONArray("columns").getJSONObject(k)
                                        // .getString("columnName")
                                        // + ":"
                                        // + columns[l].equals(table.getJSONArray("columns").getJSONObject(k)
                                        // .getString("columnName")));
                                        if (columns[l].equals(table.getJSONArray("columns").getJSONObject(k)
                                                .getString("columnName"))) {
                                            // System.out.println("\t\tFOUND COL: " + columns[l]);
                                            columns[l] = table.getJSONArray("columns").getJSONObject(k)
                                                    .getString("newValue");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                System.out.println("AHHHHHHHHHH!!!!!!!");
                System.out.println(e);
            }
            // Get first line after headings
            currentLineOfTheFile = file.nextLine();
            if (currentLineOfTheFile.charAt(currentLineOfTheFile.length() - 1) == ','
                    && countChar(currentLineOfTheFile, ',') > columns.length - 1) {
                currentLineOfTheFile = currentLineOfTheFile.substring(0, currentLineOfTheFile.length() - 1);
            }
            BA900Table currentTable = new BA900Table(tableName, this, columns);
            allTables.put(tableName, currentTable);

            // Reading in the data (keep looping until another table is found or the file
            // ends)
            while (file.hasNext() && !currentLineOfTheFile.startsWith("Table")) {
                Reader in = new StringReader(currentLineOfTheFile);
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
                currentLineOfTheFile = file.nextLine();
                if (currentLineOfTheFile.charAt(currentLineOfTheFile.length() - 1) == ','
                        && countChar(currentLineOfTheFile, ',') > columns.length - 1) {
                    currentLineOfTheFile = currentLineOfTheFile.substring(0, currentLineOfTheFile.length() - 1);
                }
            }
            Reader in = new StringReader(currentLineOfTheFile);
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
        }
        file.close();
        return allTables;
    }

    private int countChar(String toCountIn, char toCount) {
        int counter = 0;
        for (int i = 0; i < toCountIn.length(); i++) {
            if (toCountIn.charAt(i) == toCount) {
                counter++;
            }
        }
        return counter;
    }
}
