package com.clfacv.loader.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcTemplateResolver {

    private static final String PRIMARY = "primary";
    private static final String SECONDARY = "secondary";

    private final JdbcTemplate primaryJdbcTemplate;
    private final JdbcTemplate secondaryJdbcTemplate;

    public JdbcTemplateResolver(@Qualifier("jdbcTemplate") JdbcTemplate primaryJdbcTemplate,
                                @Autowired(required = false) @Qualifier("secondaryJdbcTemplate") JdbcTemplate secondaryJdbcTemplate) {
        this.primaryJdbcTemplate = primaryJdbcTemplate;
        this.secondaryJdbcTemplate = secondaryJdbcTemplate;
    }

    public JdbcTemplate resolve(String dataSource) {
        String key = normalizeKey(dataSource);

        if (PRIMARY.equals(key)) {
            return primaryJdbcTemplate;
        }

        if (SECONDARY.equals(key)) {
            if (secondaryJdbcTemplate == null) {
                throw new IllegalStateException("secondary datasource is not configured");
            }
            return secondaryJdbcTemplate;
        }

        throw new IllegalArgumentException("Unsupported datasource key: " + dataSource + ". Use primary or secondary.");
    }

    private String normalizeKey(String dataSource) {
        if (dataSource == null || dataSource.trim().isEmpty()) {
            return PRIMARY;
        }

        return dataSource.trim().toLowerCase();
    }
}
