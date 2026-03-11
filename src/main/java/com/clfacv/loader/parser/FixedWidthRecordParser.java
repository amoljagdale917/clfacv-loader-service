package com.clfacv.loader.parser;

import com.clfacv.loader.config.LoaderProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FixedWidthRecordParser {

    public List<String> parseLine(String line, List<LoaderProperties.ColumnDefinition> columns) {
        List<String> values = new ArrayList<String>(columns.size());
        int offset = 0;

        for (LoaderProperties.ColumnDefinition column : columns) {
            values.add(extractAndTrim(line, offset, column.getLength()));
            offset += column.getLength();
        }

        return values;
    }

    private String extractAndTrim(String line, int start, int length) {
        if (length <= 0) {
            return null;
        }

        if (line == null || line.length() <= start) {
            return null;
        }

        int end = Math.min(start + length, line.length());
        String raw = line.substring(start, end);
        String trimmed = raw.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}
