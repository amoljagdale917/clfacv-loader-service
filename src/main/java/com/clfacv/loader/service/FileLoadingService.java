package com.clfacv.loader.service;

import com.clfacv.loader.config.FixedWidthLayoutRegistry;
import com.clfacv.loader.config.LoaderProperties;
import com.clfacv.loader.parser.FixedWidthRecordParser;
import com.clfacv.loader.repository.FixedWidthBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
    public class FileLoadingService {

    private static final DateTimeFormatter FILE_TS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String HKHASE_FILE = "CLFACVHASE.TXT";

    private final LoaderProperties loaderProperties;
    private final FixedWidthLayoutRegistry layoutRegistry;
    private final FixedWidthRecordParser parser;
    private final FixedWidthBatchRepository repository;

    public void processConfiguredFiles() {
        if (!hasLoaderDirectoriesConfigured()) {
            log.warn("Loader path config missing. Set app.loader.input-directory, app.loader.success-directory, and app.loader.failed-directory for active profile.");
            return;
        }

        List<LoaderProperties.FileDefinition> files = loaderProperties.getFiles();

        if (files == null || files.isEmpty()) {
            log.warn("No file definitions configured in app.loader.files.");
            return;
        }

        List<ResolvedFileDefinition> validDefinitions = new ArrayList<ResolvedFileDefinition>();
        for (LoaderProperties.FileDefinition fileDefinition : files) {
            ResolvedFileDefinition resolved = resolveDefinition(fileDefinition);
            if (resolved != null) {
                validDefinitions.add(resolved);
            }
        }

        if (validDefinitions.isEmpty()) {
            log.warn("No valid file definitions found.");
            return;
        }

        clearTargetTables(validDefinitions);

        for (ResolvedFileDefinition fileDefinition : validDefinitions) {
            processSingleFile(fileDefinition);
        }
    }

    private ResolvedFileDefinition resolveDefinition(LoaderProperties.FileDefinition fileDefinition) {
        if (fileDefinition.getFileName() == null || fileDefinition.getFileName().trim().isEmpty()) {
            log.warn("Skipping file definition with empty file-name.");
            return null;
        }

        if (fileDefinition.getTableName() == null || fileDefinition.getTableName().trim().isEmpty()) {
            log.warn("Skipping {} due to empty table-name.", fileDefinition.getFileName());
            return null;
        }

        List<LoaderProperties.ColumnDefinition> columns = layoutRegistry.getColumnsForFile(fileDefinition.getFileName());
        if (columns.isEmpty()) {
            log.warn("Skipping {} because no static layout is configured for this file name.", fileDefinition.getFileName());
            return null;
        }

        for (LoaderProperties.ColumnDefinition column : columns) {
            if (column.getName() == null || column.getName().trim().isEmpty() || column.getLength() <= 0) {
                log.warn("Skipping {} due to invalid static layout configuration.", fileDefinition.getFileName());
                return null;
            }
        }

        return new ResolvedFileDefinition(fileDefinition, columns);
    }

    void processSingleFile(ResolvedFileDefinition resolved) {
        LoaderProperties.FileDefinition fileDefinition = resolved.getDefinition();
        List<LoaderProperties.ColumnDefinition> columns = resolved.getColumns();
        Path filePath = Paths.get(loaderProperties.getInputDirectory(), fileDefinition.getFileName());

        if (!Files.exists(filePath)) {
            log.warn("Input file not found: {}", filePath);
            return;
        }

        boolean processingSuccess = false;
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

                rows.add(parser.parseLine(line, columns));

                if (rows.size() >= batchSize) {
                    totalInserted += flushBatch(fileDefinition, columns, rows);
                }
            }

            totalInserted += flushBatch(fileDefinition, columns, rows);

            log.info("Finished {} -> {}. Lines read: {}, rows inserted: {}",
                    fileDefinition.getFileName(), fileDefinition.getTableName(), totalLines, totalInserted);
            processingSuccess = true;
        } catch (Exception ex) {
            log.error("Error while processing file {}", filePath, ex);
        } finally {
            moveProcessedFile(filePath, processingSuccess);
        }
    }

    private boolean hasLoaderDirectoriesConfigured() {
        return hasText(loaderProperties.getInputDirectory())
                && hasText(loaderProperties.getSuccessDirectory())
                && hasText(loaderProperties.getFailedDirectory());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void clearTargetTables(List<ResolvedFileDefinition> fileDefinitions) {
        Set<String> processed = new HashSet<String>();

        for (ResolvedFileDefinition resolved : fileDefinitions) {
            LoaderProperties.FileDefinition definition = resolved.getDefinition();
            String tableName = definition.getTableName().trim();

            if (!processed.add(tableName)) {
                continue;
            }

            int beforeCount = repository.countAll(tableName);
            if (beforeCount == 0) {
                log.info("Skip delete for {} because row count is 0.", tableName);
                continue;
            }

            int deletedCount = repository.deleteAll(tableName);
            if (deletedCount != beforeCount) {
                throw new IllegalStateException("Delete count mismatch for table " + tableName
                        + ". Count before delete: " + beforeCount
                        + ", delete returned: " + deletedCount);
            }

            int afterCount = repository.countAll(tableName);
            if (afterCount != 0) {
                throw new IllegalStateException("Table " + tableName
                        + " is not empty after delete. Remaining row count: " + afterCount);
            }

            log.info("Deleted {} rows from {} before load", deletedCount, tableName);
        }
    }

    private String resolveRegionByFileName(String fileName) {
        if (fileName != null) {
            String normalized = fileName.trim().toUpperCase();
            if (HKHASE_FILE.equals(normalized) || normalized.contains("HASE")) {
                return "HKHASE";
            }
        }

        return "HK";
    }

    private int flushBatch(LoaderProperties.FileDefinition fileDefinition,
                           List<LoaderProperties.ColumnDefinition> columns,
                           List<List<String>> rows) {
        if (rows.isEmpty()) {
            return 0;
        }

        repository.saveBatch(
                fileDefinition.getTableName(),
                columns,
                rows,
                resolveRegionByFileName(fileDefinition.getFileName()));

        int inserted = rows.size();
        rows.clear();
        return inserted;
    }

    private void moveProcessedFile(Path sourceFile, boolean successful) {
        String targetDirectory = successful ? loaderProperties.getSuccessDirectory() : loaderProperties.getFailedDirectory();
        if (targetDirectory == null || targetDirectory.trim().isEmpty()) {
            log.warn("Cannot move {} because target directory is empty.", sourceFile.getFileName());
            return;
        }

        Path targetDirPath = Paths.get(targetDirectory);
        String stampedName = buildStampedFileName(sourceFile.getFileName().toString());
        Path targetFile = targetDirPath.resolve(stampedName);

        try {
            Files.createDirectories(targetDirPath);
            Files.move(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Moved {} to {}", sourceFile.getFileName(), targetFile);
        } catch (IOException ex) {
            log.error("Failed to move {} to {}", sourceFile.getFileName(), targetFile, ex);
        }
    }

    private String buildStampedFileName(String originalFileName) {
        String timestamp = LocalDateTime.now().format(FILE_TS_FORMAT);
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == originalFileName.length() - 1) {
            return originalFileName + "_" + timestamp;
        }

        String baseName = originalFileName.substring(0, dotIndex);
        String extension = originalFileName.substring(dotIndex);
        return baseName + "_" + timestamp + extension;
    }

    private static class ResolvedFileDefinition {

        private final LoaderProperties.FileDefinition definition;
        private final List<LoaderProperties.ColumnDefinition> columns;

        private ResolvedFileDefinition(LoaderProperties.FileDefinition definition,
                                       List<LoaderProperties.ColumnDefinition> columns) {
            this.definition = definition;
            this.columns = columns;
        }

        private LoaderProperties.FileDefinition getDefinition() {
            return definition;
        }

        private List<LoaderProperties.ColumnDefinition> getColumns() {
            return columns;
        }
    }
}
