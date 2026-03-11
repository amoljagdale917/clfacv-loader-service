package com.clfacv.loader.repository;

import com.clfacv.loader.config.LoaderProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Repository
@RequiredArgsConstructor
public class FixedWidthBatchRepository {

    private static final Pattern SQL_IDENTIFIER = Pattern.compile("[A-Za-z][A-Za-z0-9_$#.]*");

    private final JdbcTemplateResolver jdbcTemplateResolver;

    public void saveBatch(String dataSource,
                          String tableName,
                          List<LoaderProperties.ColumnDefinition> columns,
                          List<List<String>> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }

        String insertSql = buildInsertSql(tableName, columns);
        JdbcTemplate jdbcTemplate = jdbcTemplateResolver.resolve(dataSource);

        jdbcTemplate.batchUpdate(insertSql, rows, rows.size(), this::mapRow);
    }

    private String buildInsertSql(String tableName, List<LoaderProperties.ColumnDefinition> columns) {
        validateIdentifier(tableName, "tableName");

        List<String> columnNames = new ArrayList<String>(columns.size());
        List<String> placeholders = new ArrayList<String>(columns.size());

        for (LoaderProperties.ColumnDefinition column : columns) {
            String columnName = column.getName();
            validateIdentifier(columnName, "columnName");

            columnNames.add(columnName);
            placeholders.add("?");
        }

        return "INSERT INTO " + tableName + " (" + String.join(", ", columnNames)
                + ") VALUES (" + String.join(", ", placeholders) + ")";
    }

    private void validateIdentifier(String identifier, String label) {
        if (identifier == null || !SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid " + label + ": " + identifier);
        }
    }

    private void mapRow(PreparedStatement ps, List<String> row) throws SQLException {
        for (int i = 0; i < row.size(); i++) {
            String value = row.get(i);
            int parameterIndex = i + 1;

            if (value == null) {
                ps.setNull(parameterIndex, Types.VARCHAR);
            } else {
                ps.setString(parameterIndex, value);
            }
        }
    }
}
