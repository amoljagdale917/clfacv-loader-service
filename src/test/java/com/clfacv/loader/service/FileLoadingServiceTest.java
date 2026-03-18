package com.clfacv.loader.service;

import com.clfacv.loader.config.FixedWidthLayoutRegistry;
import com.clfacv.loader.config.LoaderProperties;
import com.clfacv.loader.parser.FixedWidthRecordParser;
import com.clfacv.loader.repository.FixedWidthBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileLoadingServiceTest {

    private static final String FILE_NAME = "CLFACV.TXT";
    private static final String TABLE_NAME = "STG_HK_OBS_FACVDW";

    @Mock
    private FixedWidthLayoutRegistry layoutRegistry;
    @Mock
    private FixedWidthRecordParser parser;
    @Mock
    private FixedWidthBatchRepository repository;

    private FileLoadingService service;

    @BeforeEach
    void setUp() {
        LoaderProperties properties = new LoaderProperties();
        properties.setInputDirectory("target/non-existent-input");
        properties.setSuccessDirectory("target/non-existent-success");
        properties.setFailedDirectory("target/non-existent-failed");
        properties.setFiles(Collections.singletonList(fileDefinition(FILE_NAME, TABLE_NAME)));

        when(layoutRegistry.getColumnsForFile(FILE_NAME)).thenReturn(validColumns());

        service = new FileLoadingService(properties, layoutRegistry, parser, repository);
    }

    @Test
    void processConfiguredFiles_whenCountIsZero_shouldSkipDelete() {
        when(repository.countAll(TABLE_NAME)).thenReturn(0);

        assertDoesNotThrow(() -> service.processConfiguredFiles());

        verify(repository, times(1)).countAll(TABLE_NAME);
        verify(repository, never()).deleteAll(TABLE_NAME);
    }

    @Test
    void processConfiguredFiles_whenCountIsTen_shouldDeleteTen() {
        when(repository.countAll(TABLE_NAME)).thenReturn(10, 0);
        when(repository.deleteAll(TABLE_NAME)).thenReturn(10);

        assertDoesNotThrow(() -> service.processConfiguredFiles());

        verify(repository, times(2)).countAll(TABLE_NAME);
        verify(repository, times(1)).deleteAll(TABLE_NAME);
    }

    @Test
    void processConfiguredFiles_whenDeleteCountMismatch_shouldThrow() {
        when(repository.countAll(TABLE_NAME)).thenReturn(10);
        when(repository.deleteAll(TABLE_NAME)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.processConfiguredFiles());

        verify(repository, times(1)).countAll(TABLE_NAME);
        verify(repository, times(1)).deleteAll(TABLE_NAME);
    }

    private LoaderProperties.FileDefinition fileDefinition(String fileName, String tableName) {
        LoaderProperties.FileDefinition definition = new LoaderProperties.FileDefinition();
        definition.setFileName(fileName);
        definition.setTableName(tableName);
        return definition;
    }

    private List<LoaderProperties.ColumnDefinition> validColumns() {
        List<LoaderProperties.ColumnDefinition> columns = new ArrayList<LoaderProperties.ColumnDefinition>();
        LoaderProperties.ColumnDefinition column = new LoaderProperties.ColumnDefinition();
        column.setName("BNK_NO");
        column.setLength(3);
        columns.add(column);
        return columns;
    }
}

