package com.clfacv.loader.repository;

import com.clfacv.loader.config.LoaderProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Repository
@RequiredArgsConstructor
public class FixedWidthBatchRepository {

    private static final Pattern SQL_IDENTIFIER = Pattern.compile("[A-Za-z][A-Za-z0-9_$#.]*");
    private static final String FACV_TABLE = "STG_HK_OBS_FACVDW";
    private static final String IMTM_TABLE = "STG_HK_OBS_IMTM";
    private static final List<String> IMTM_EXTRA_COLUMNS = Arrays.asList("REGION", "BATCH_RUN_ID");
    private static final List<String> IMTM_EXTRA_VALUES = Arrays.asList("?", "1");

    private final JdbcTemplate jdbcTemplate;

    public int deleteAll(String tableName) {
        validateIdentifier(tableName, "tableName");
        return jdbcTemplate.update("DELETE FROM " + tableName);
    }

    public int countAll(String tableName) {
        validateIdentifier(tableName, "tableName");
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM " + tableName, Integer.class);
        return count == null ? 0 : count.intValue();
    }

    public void saveBatch(String tableName,
                          List<LoaderProperties.ColumnDefinition> columns,
                          List<List<String>> rows,
                          String region) {
        if (rows == null || rows.isEmpty()) {
            return;
        }

        if (isFacvTargetTable(tableName)) {
            saveFacvBatch(tableName, columns, rows, region);
            return;
        }

        if (isImtmTargetTable(tableName)) {
            saveImtmBatch(tableName, columns, rows, region);
            return;
        }

        String insertSql = buildGenericInsertSql(tableName, columns);
        jdbcTemplate.batchUpdate(insertSql, rows, rows.size(), this::mapGenericRow);
    }

    private void saveFacvBatch(String tableName,
                               List<LoaderProperties.ColumnDefinition> columns,
                               List<List<String>> rows,
                               String region) {
        String insertSql = buildFacvInsertSql(tableName);
        Map<String, Integer> columnIndexes = buildColumnIndexes(columns);
        String normalizedRegion = normalizeRegion(region);

        jdbcTemplate.batchUpdate(insertSql, rows, rows.size(),
                (ps, row) -> mapFacvRow(ps, row, columnIndexes, normalizedRegion));
    }

    private void saveImtmBatch(String tableName,
                               List<LoaderProperties.ColumnDefinition> columns,
                               List<List<String>> rows,
                               String region) {
        String insertSql = buildImtmInsertSql(tableName, columns);
        String normalizedRegion = normalizeRegion(region);
        jdbcTemplate.batchUpdate(insertSql, rows, rows.size(),
                (ps, row) -> mapImtmRow(ps, row, normalizedRegion));
    }

    private String buildGenericInsertSql(String tableName, List<LoaderProperties.ColumnDefinition> columns) {
        return buildInsertSql(tableName, columns, Collections.<String>emptyList(), Collections.<String>emptyList());
    }

    private String buildImtmInsertSql(String tableName, List<LoaderProperties.ColumnDefinition> columns) {
        validateIdentifier(tableName, "tableName");
        return buildInsertSql(tableName, columns, IMTM_EXTRA_COLUMNS, IMTM_EXTRA_VALUES);
    }

    private String buildInsertSql(String tableName,
                                  List<LoaderProperties.ColumnDefinition> columns,
                                  List<String> extraColumns,
                                  List<String> extraValues) {
        validateIdentifier(tableName, "tableName");

        int extraCount = extraColumns == null ? 0 : extraColumns.size();
        List<String> columnNames = new ArrayList<String>(columns.size() + extraCount);
        List<String> placeholders = new ArrayList<String>(columns.size() + extraCount);

        for (LoaderProperties.ColumnDefinition column : columns) {
            String columnName = column.getName();
            validateIdentifier(columnName, "columnName");

            columnNames.add(columnName);
            placeholders.add("?");
        }

        if (extraCount > 0) {
            if (extraValues == null || extraColumns.size() != extraValues.size()) {
                throw new IllegalArgumentException("extraColumns and extraValues size mismatch");
            }

            for (int i = 0; i < extraColumns.size(); i++) {
                String extraColumn = extraColumns.get(i);
                String extraValue = extraValues.get(i);
                validateIdentifier(extraColumn, "columnName");
                columnNames.add(extraColumn);
                placeholders.add(extraValue);
            }
        }

        return "INSERT INTO " + tableName + " (" + String.join(", ", columnNames)
                + ") VALUES (" + String.join(", ", placeholders) + ")";
    }

    private String buildFacvInsertSql(String tableName) {
        validateIdentifier(tableName, "tableName");
        return "INSERT INTO " + tableName + " ("
                + "BNK_NO, CUST_ACCT_NO, SYS_COD, ITL_CUST_NO, CUST_ID, FILLER1, "
                + "REC_TYPE, TREE_ID, LMT_ID, MAINT_ACT, REGION, BATCH_RUN_ID"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";
    }

    private void validateIdentifier(String identifier, String label) {
        if (identifier == null || !SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid " + label + ": " + identifier);
        }
    }

    private void mapGenericRow(PreparedStatement ps, List<String> row) throws SQLException {
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

    private void mapFacvRow(PreparedStatement ps,
                            List<String> row,
                            Map<String, Integer> columnIndexes,
                            String region) throws SQLException {
        String bnkNo = valueByName(row, columnIndexes, "BNK_NO");
        String custAcctNo = valueByName(row, columnIndexes, "CUST_ACCT_NO");
        String sysCod = valueByName(row, columnIndexes, "SYS_COD");
        String recType = valueByName(row, columnIndexes, "REC_TYPE");
        String custGp = valueByName(row, columnIndexes, "CUST_GP");
        String itlCustNo = valueByName(row, columnIndexes, "ITL_CUST_NO");
        String filler = valueByName(row, columnIndexes, "FILLER");
        String lmtId = valueByName(row, columnIndexes, "LMT_ID");
        String custId = valueByName(row, columnIndexes, "CUST_ID");
        String filler1 = valueByName(row, columnIndexes, "FILLER1");
        String maintAct = valueByName(row, columnIndexes, "MAINT_ACT");
        String treeId = buildTreeId(recType, custGp, itlCustNo, filler);

        setStringOrNull(ps, 1, bnkNo);
        setStringOrNull(ps, 2, custAcctNo);
        setStringOrNull(ps, 3, sysCod);
        setStringOrNull(ps, 4, itlCustNo);
        setStringOrNull(ps, 5, custId);
        setStringOrNull(ps, 6, filler1);
        setStringOrNull(ps, 7, recType);
        setStringOrNull(ps, 8, treeId);
        setStringOrNull(ps, 9, lmtId);
        setStringOrNull(ps, 10, maintAct);
        setStringOrNull(ps, 11, region);
    }

    private void mapImtmRow(PreparedStatement ps,
                            List<String> row,
                            String region) throws SQLException {
        mapGenericRow(ps, row);
        setStringOrNull(ps, row.size() + 1, region);
    }

    private Map<String, Integer> buildColumnIndexes(List<LoaderProperties.ColumnDefinition> columns) {
        Map<String, Integer> indexes = new HashMap<String, Integer>(columns.size());
        for (int i = 0; i < columns.size(); i++) {
            String columnName = columns.get(i).getName();
            if (columnName != null && !columnName.trim().isEmpty()) {
                indexes.put(columnName.trim().toUpperCase(Locale.ROOT), Integer.valueOf(i));
            }
        }
        return indexes;
    }

    private String valueByName(List<String> row, Map<String, Integer> columnIndexes, String columnName) {
        Integer index = columnIndexes.get(columnName);
        if (index == null) {
            return null;
        }

        int position = index.intValue();
        if (position < 0 || position >= row.size()) {
            return null;
        }

        return row.get(position);
    }

    private String buildTreeId(String recType, String custGp, String itlCustNo, String filler) {
        return joinNonBlank(recType, custGp, itlCustNo, filler);
    }

    private String joinNonBlank(String... values) {
        if (values == null || values.length == 0) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value == null) {
                continue;
            }

            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            builder.append(trimmed);
        }

        return builder.length() == 0 ? null : builder.toString();
    }

    private boolean isFacvTargetTable(String tableName) {
        return isTargetTable(tableName, FACV_TABLE);
    }

    private boolean isImtmTargetTable(String tableName) {
        return isTargetTable(tableName, IMTM_TABLE);
    }

    private boolean isTargetTable(String tableName, String expectedTableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return false;
        }

        String normalized = tableName.trim().toUpperCase(Locale.ROOT);
        return expectedTableName.equals(normalized);
    }

    private String normalizeRegion(String region) {
        if (region == null) {
            return null;
        }

        String normalized = region.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void setStringOrNull(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }
}
