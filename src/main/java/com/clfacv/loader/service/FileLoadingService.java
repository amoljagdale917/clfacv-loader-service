package com.clfacv.loader.service;

import com.clfacv.loader.config.LoaderProperties;
import com.clfacv.loader.parser.FixedWidthRecordParser;
import com.clfacv.loader.repository.FixedWidthBatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileLoadingService {

    private static final Logger log = LoggerFactory.getLogger(FileLoadingService.class);

    private final LoaderProperties loaderProperties;
    private final FixedWidthRecordParser parser;
    private final FixedWidthBatchRepository repository;

    public FileLoadingService(LoaderProperties loaderProperties,
                              FixedWidthRecordParser parser,
                              FixedWidthBatchRepository repository) {
        this.loaderProperties = loaderProperties;
        this.parser = parser;
        this.repository = repository;
    }

    public void processConfiguredFiles() {
        List<LoaderProperties.FileDefinition> files = loaderProperties.getFiles();

        if (files == null || files.isEmpty()) {
            log.warn("No file definitions configured in app.loader.files.");
            return;
        }

        for (LoaderProperties.FileDefinition fileDefinition : files) {
            if (!isValidDefinition(fileDefinition)) {
                continue;
            }

            processSingleFile(fileDefinition);
        }
    }

    private boolean isValidDefinition(LoaderProperties.FileDefinition fileDefinition) {
        if (fileDefinition.getFileName() == null || fileDefinition.getFileName().trim().isEmpty()) {
            log.warn("Skipping file definition with empty file-name.");
            return false;
        }

        if (fileDefinition.getTableName() == null || fileDefinition.getTableName().trim().isEmpty()) {
            log.warn("Skipping {} due to empty table-name.", fileDefinition.getFileName());
            return false;
        }

        if (fileDefinition.getColumns() == null || fileDefinition.getColumns().isEmpty()) {
            log.warn("Skipping {} due to missing columns.", fileDefinition.getFileName());
            return false;
        }

        if (fileDefinition.getDataSource() != null
                && !fileDefinition.getDataSource().trim().isEmpty()
                && !isSupportedDataSource(fileDefinition.getDataSource())) {
            log.warn("Skipping {} due to unsupported data-source {}. Use primary or secondary.",
                    fileDefinition.getFileName(), fileDefinition.getDataSource());
            return false;
        }

        for (LoaderProperties.ColumnDefinition column : fileDefinition.getColumns()) {
            if (column.getName() == null || column.getName().trim().isEmpty() || column.getLength() <= 0) {
                log.warn("Skipping {} due to invalid column configuration.", fileDefinition.getFileName());
                return false;
            }
        }

        return true;
    }

    void processSingleFile(LoaderProperties.FileDefinition fileDefinition) {
        Path filePath = Paths.get(loaderProperties.getInputDirectory(), fileDefinition.getFileName());

        if (!Files.exists(filePath)) {
            log.warn("Input file not found: {}", filePath);
            return;
        }

        int totalInserted = 0;
        int totalLines = 0;
        int batchSize = Math.max(loaderProperties.getBatchSize(), 1);
        List<List<String>> rows = new ArrayList<List<String>>(batchSize);

        try (BufferedReader reader = Files.newBufferedReader(filePath, Charset.forName(loaderProperties.getInputCharset()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                totalLines++;
                if (line.trim().isEmpty()) {
                    continue;
                }

                rows.add(parser.parseLine(line, fileDefinition.getColumns()));

                if (rows.size() >= batchSize) {
                    repository.saveBatch(fileDefinition.getDataSource(), fileDefinition.getTableName(), fileDefinition.getColumns(), rows);
                    totalInserted += rows.size();
                    rows.clear();
                }
            }

            if (!rows.isEmpty()) {
                repository.saveBatch(fileDefinition.getDataSource(), fileDefinition.getTableName(), fileDefinition.getColumns(), rows);
                totalInserted += rows.size();
            }

            log.info("Finished {} -> {} (datasource={}). Lines read: {}, rows inserted: {}",
                    fileDefinition.getFileName(), fileDefinition.getTableName(), defaultDataSource(fileDefinition.getDataSource()), totalLines, totalInserted);
        } catch (IOException ex) {
            log.error("Error while processing file {}", filePath, ex);
        }
    }

    private String defaultDataSource(String dataSource) {
        if (dataSource == null || dataSource.trim().isEmpty()) {
            return "primary";
        }
        return dataSource.trim();
    }

    private boolean isSupportedDataSource(String dataSource) {
        String key = dataSource.trim().toLowerCase();
        return "primary".equals(key) || "secondary".equals(key);
    }
}
