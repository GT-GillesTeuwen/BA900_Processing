package com.gilles.Core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.plaf.basic.BasicBorders;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.gilles.DataClasses.BA900Record;
import com.gilles.Exceptions.InvalidRecordException;

public class DataStore {
    HashMap<String, HashMap<YearMonth, BA900Record>> allRecords;

    public DataStore(String dir) {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        this.allRecords = new HashMap<>();
        final File folder = new File(dir);
        listFilesForFolder(folder);
    }

    public void print() {
        for (String inst : allRecords.keySet()) {
            System.out.println(inst);
        }
    }

    public HashMap<String, HashMap<YearMonth, BA900Record>> getAllRecords() {
        return allRecords;
    }

    private void listFilesForFolder(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                addBA900Record(fileEntry);
            }
        }
    }

    private void addBA900Record(File fileEntry) {
        if (!fileEntry.getName().contains(".csv")) {
            return;
        }
        try {
            Scanner file = new Scanner(new FileReader(fileEntry));
            YearMonth yearMonth = getYearMonthOfFile(file);
            String institution = getInstitutionOfFile(file);
            String formType = getFormTypeOfFile(file);

            BA900Record currentRecord = new BA900Record(yearMonth, institution, formType, fileEntry.getAbsolutePath());
            allRecords.putIfAbsent(institution, new HashMap<YearMonth, BA900Record>());
            allRecords.get(institution).put(yearMonth, currentRecord);
            file.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private YearMonth getYearMonthOfFile(Scanner file) {
        String cur = "";
        while (!cur.startsWith("Date")) {
            cur = file.nextLine();
        }
        String dateStr = cur.split(",")[1];

        DateTimeFormatter format1 = DateTimeFormatter.ofPattern("MMMM yyyy");
        DateTimeFormatter format2 = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter format3 = DateTimeFormatter.ofPattern("MMM-yy");
        DateTimeFormatter format4 = DateTimeFormatter.ofPattern("yy-MMM");
        try {
            YearMonth yearMonth = YearMonth.parse(dateStr, format1);
            return yearMonth;
        } catch (DateTimeParseException dateTimeParseException) {
            try {
                YearMonth yearMonth = YearMonth.parse(dateStr, format2);
                return yearMonth;
            } catch (DateTimeParseException dateTimeParseException2) {
                try {
                    YearMonth yearMonth = YearMonth.parse(dateStr, format3);
                    return yearMonth;
                } catch (DateTimeException dataTimeException3) {
                    try {
                        YearMonth yearMonth = YearMonth.parse(dateStr, format4);
                        return yearMonth;
                    } catch (DateTimeException dataTimeException4) {
                        return null;
                    }
                }
            }
        }

    }

    private String getInstitutionOfFile(Scanner file) {
        String cur = "";
        while (!cur.startsWith("Institution")) {
            cur = file.nextLine();
        }
        String institutionStr = cur.split(",")[1];
        int bracketIndex = institutionStr.indexOf('(', 0);
        if (bracketIndex != -1) {
            institutionStr = institutionStr.substring(0, bracketIndex);
        }
        institutionStr = institutionStr.trim();
        return institutionStr;
    }

    private String getFormTypeOfFile(Scanner file) {
        String cur = "";
        while (!cur.startsWith("Form Type")) {
            cur = file.nextLine();
        }
        String formType = cur.split(",")[1];
        return formType;
    }

}
