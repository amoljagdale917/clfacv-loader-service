package com.clfacv.loader.parser;

import com.clfacv.loader.config.LoaderProperties;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FixedWidthRecordParserTest {

    private final FixedWidthRecordParser parser = new FixedWidthRecordParser();

    @Test
    void parseLine_shouldMapAndTrimFields() {
        List<LoaderProperties.ColumnDefinition> columns = columns(3, 12, 3, 1, 1, 11, 11, 5, 11, 7, 1);

        String line = "004"
                + "065017162001"
                + "CIF"
                + " "
                + "C"
                + "00000000072"
                + "    00000  "
                + "DOD01"
                + "00000000263"
                + " A B   "
                + "D";

        List<String> values = parser.parseLine(line, columns);

        assertEquals("004", values.get(0));
        assertEquals("065017162001", values.get(1));
        assertEquals("CIF", values.get(2));
        assertNull(values.get(3));
        assertEquals("C", values.get(4));
        assertEquals("00000000072", values.get(5));
        assertEquals("00000", values.get(6));
        assertEquals("DOD01", values.get(7));
        assertEquals("00000000263", values.get(8));
        assertEquals("A B", values.get(9));
        assertEquals("D", values.get(10));
    }

    @Test
    void parseLine_shouldReturnNullForMissingTail() {
        List<LoaderProperties.ColumnDefinition> columns = columns(3, 12, 3, 1);
        List<String> values = parser.parseLine("004065017162001", columns);

        assertEquals("004", values.get(0));
        assertEquals("065017162001", values.get(1));
        assertNull(values.get(2));
        assertNull(values.get(3));
    }

    private List<LoaderProperties.ColumnDefinition> columns(Integer... lengths) {
        List<LoaderProperties.ColumnDefinition> columns = new ArrayList<LoaderProperties.ColumnDefinition>();

        for (Integer length : Arrays.asList(lengths)) {
            LoaderProperties.ColumnDefinition column = new LoaderProperties.ColumnDefinition();
            column.setName("COL_" + columns.size());
            column.setLength(length);
            columns.add(column);
        }

        return columns;
    }
}
