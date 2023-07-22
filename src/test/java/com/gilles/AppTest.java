package com.gilles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import com.gilles.Core.DataStore;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     * 
     * @throws IOException
     */
    @Test
    public void shouldAnswerWithTrue() throws IOException {
        Reader in = new StringReader("\"NCD's/PN's\",4,,,,,,,,");
        CSVParser p = CSVFormat.DEFAULT.parse(in);
        assertEquals(10, p.getRecords().get(0).size());
    }

    @Test
    public void meep() throws IOException {
        DataStore store = new DataStore("./Data");
        store.print();
    }

    @Test
    public void meep2() throws IOException {
        String s = "adfjla;sdk asldkfja ";
        int b = s.indexOf('(', 0);
        System.out.println(b);
    }

    @Test
    public void meep3() throws IOException {
        String s = "012345";
        s = s.substring(0, s.length() - 1);
        System.out.println(s);
    }

}
