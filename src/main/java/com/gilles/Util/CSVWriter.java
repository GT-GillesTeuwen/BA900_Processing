package com.gilles.Util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CSVWriter {
    String[] cols;
    ArrayList<String[]> records;

    public CSVWriter(String[] cols) {
        this.cols = cols;
        records = new ArrayList<>();
    }

    public void addRecord(String[] rec) {
        records.add(rec);
    }

    public void write(String bank) {
        try {
            FileWriter writer = new FileWriter(bank + ".csv", false);
            String out = "";
            for (int i = 0; i < cols.length; i++) {
                out += cols[i];
                if (i < cols.length - 1) {
                    out += ",";
                }
            }
            out += "\n";
            for (String[] record : records) {
                for (int i = 0; i < record.length; i++) {
                    out += record[i];
                    if (i < record.length - 1) {
                        out += ",";
                    }
                }
                out += "\n";
            }
            writer.write(out);
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}