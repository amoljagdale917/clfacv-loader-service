package com.clfacv.loader.service;

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
    private static final String HK_FILE = "CLFACV.TXT";
    private static final String HKHASE_FILE = "CLFACVHASE.TXT";

    private final LoaderProperties loaderProperties;
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

        List<LoaderProperties.FileDefinition> validDefinitions = new ArrayList<LoaderProperties.FileDefinition>();
        for (LoaderProperties.FileDefinition fileDefinition : files) {
            if (!isValidDefinition(fileDefinition)) {
                continue;
            }
            validDefinitions.add(fileDefinition);
        }

        if (validDefinitions.isEmpty()) {
            log.warn("No valid file definitions found.");
            return;
        }

        clearTargetTables(validDefinitions);

        for (LoaderProperties.FileDefinition fileDefinition : validDefinitions) {
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

                rows.add(parser.parseLine(line, fileDefinition.getColumns()));

                if (rows.size() >= batchSize) {
                    repository.saveBatch(
                            fileDefinition.getDataSource(),
                            fileDefinition.getTableName(),
                            fileDefinition.getColumns(),
                            rows,
                            resolveRegionByFileName(fileDefinition.getFileName()));
                    totalInserted += rows.size();
                    rows.clear();
                }
            }

            if (!rows.isEmpty()) {
                repository.saveBatch(
                        fileDefinition.getDataSource(),
                        fileDefinition.getTableName(),
                        fileDefinition.getColumns(),
                        rows,
                        resolveRegionByFileName(fileDefinition.getFileName()));
                totalInserted += rows.size();
            }

            log.info("Finished {} -> {} (datasource={}). Lines read: {}, rows inserted: {}",
                    fileDefinition.getFileName(), fileDefinition.getTableName(), defaultDataSource(fileDefinition.getDataSource()), totalLines, totalInserted);
            processingSuccess = true;
        } catch (Exception ex) {
            log.error("Error while processing file {}", filePath, ex);
        } finally {
            moveProcessedFile(filePath, processingSuccess);
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

    private boolean hasLoaderDirectoriesConfigured() {
        return hasText(loaderProperties.getInputDirectory())
                && hasText(loaderProperties.getSuccessDirectory())
                && hasText(loaderProperties.getFailedDirectory());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void clearTargetTables(List<LoaderProperties.FileDefinition> fileDefinitions) {
        Set<String> processed = new HashSet<String>();

        for (LoaderProperties.FileDefinition definition : fileDefinitions) {
            String dataSource = defaultDataSource(definition.getDataSource());
            String tableName = definition.getTableName().trim();
            String key = dataSource + "|" + tableName;

            if (!processed.add(key)) {
                continue;
            }

            int deletedCount = repository.deleteAll(dataSource, tableName);
            log.info("Deleted {} rows from {} before load (datasource={})", deletedCount, tableName, dataSource);
        }
    }

    private String resolveRegionByFileName(String fileName) {
        if (fileName == null) {
            return "HK";
        }

        String normalized = fileName.trim().toUpperCase();
        if (HKHASE_FILE.equals(normalized) || normalized.contains("HASE")) {
            return "HKHASE";
        }

        if (HK_FILE.equals(normalized)) {
            return "HK";
        }

        return "HK";
    }

    private void moveProcessedFile(Path sourceFile, boolean successful) {
        String targetDirectory = successful ? loaderProperties.getSuccessDirectory() : loaderProperties.getFailedDirectory();
        if (targetDirectory == null || targetDirectory.trim().isEmpty()) {
            log.warn("Cannot move {} because target directory is empty.", sourceFile.getFileName());
            return;
        }

        Path targetDirPath = Paths.get(targetDirectory);
        String stampedName = sourceFile.getFileName().toString() + "_" + LocalDateTime.now().format(FILE_TS_FORMAT);
        Path targetFile = targetDirPath.resolve(stampedName);

        try {
            Files.createDirectories(targetDirPath);
            Files.move(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Moved {} to {}", sourceFile.getFileName(), targetFile);
        } catch (IOException ex) {
            log.error("Failed to move {} to {}", sourceFile.getFileName(), targetFile, ex);
        }
    }
}
