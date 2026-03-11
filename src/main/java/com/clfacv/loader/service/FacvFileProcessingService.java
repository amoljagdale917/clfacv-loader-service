package com.clfacv.loader.service;

import com.clfacv.loader.model.FacvRecord;
import com.clfacv.loader.parser.FixedWidthFacvParser;
import com.clfacv.loader.repository.FacvRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacvFileProcessingService {

    private static final Logger log = LoggerFactory.getLogger(FacvFileProcessingService.class);

    private final FixedWidthFacvParser parser;
    private final FacvRecordRepository repository;

    @Value("${app.input.directory:lms/file/input}")
    private String inputDirectory;

    @Value("${app.input.files:CLFACV.TXT,CLFACVHASE.TXT}")
    private String inputFiles;

    @Value("${app.input.charset:UTF-8}")
    private String inputCharset;

    @Value("${app.db.batch-size:500}")
    private int batchSize;

    public FacvFileProcessingService(FixedWidthFacvParser parser, FacvRecordRepository repository) {
        this.parser = parser;
        this.repository = repository;
    }

    public void processConfiguredFiles() {
        List<String> files = Arrays.stream(inputFiles.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());

        if (files.isEmpty()) {
            log.warn("No input files configured. Please set app.input.files.");
            return;
        }

        for (String fileName : files) {
            processSingleFile(Paths.get(inputDirectory, fileName));
        }
    }

    void processSingleFile(Path filePath) {
        if (!Files.exists(filePath)) {
            log.warn("Input file not found: {}", filePath);
            return;
        }

        int totalInserted = 0;
        int totalLines = 0;
        List<FacvRecord> records = new ArrayList<>(batchSize);

        try (BufferedReader reader = Files.newBufferedReader(filePath, Charset.forName(inputCharset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                totalLines++;
                if (line.trim().isEmpty()) {
                    continue;
                }

                records.add(parser.parseLine(line));

                if (records.size() >= batchSize) {
                    repository.saveBatch(records);
                    totalInserted += records.size();
                    records.clear();
                }
            }

            if (!records.isEmpty()) {
                repository.saveBatch(records);
                totalInserted += records.size();
            }

            log.info("Finished {}. Lines read: {}, rows inserted: {}", filePath.getFileName(), totalLines, totalInserted);
        } catch (IOException ex) {
            log.error("Error while processing file {}", filePath, ex);
        }
    }
}
