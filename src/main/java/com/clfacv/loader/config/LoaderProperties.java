package com.clfacv.loader.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.loader")
public class LoaderProperties {

    private String inputDirectory = "lms/file/input";
    private String successDirectory = "lms/file/success";
    private String failedDirectory = "lms/file/failed";
    private String inputCharset = "UTF-8";
    private int batchSize = 500;
    private List<FileDefinition> files = new ArrayList<FileDefinition>();

    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public String getInputCharset() {
        return inputCharset;
    }

    public void setInputCharset(String inputCharset) {
        this.inputCharset = inputCharset;
    }

    public String getSuccessDirectory() {
        return successDirectory;
    }

    public void setSuccessDirectory(String successDirectory) {
        this.successDirectory = successDirectory;
    }

    public String getFailedDirectory() {
        return failedDirectory;
    }

    public void setFailedDirectory(String failedDirectory) {
        this.failedDirectory = failedDirectory;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public List<FileDefinition> getFiles() {
        return files;
    }

    public void setFiles(List<FileDefinition> files) {
        this.files = files;
    }

    public static class FileDefinition {

        private String fileName;
        private String dataSource;
        private String tableName;
        private List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getDataSource() {
            return dataSource;
        }

        public void setDataSource(String dataSource) {
            this.dataSource = dataSource;
        }

        public List<ColumnDefinition> getColumns() {
            return columns;
        }

        public void setColumns(List<ColumnDefinition> columns) {
            this.columns = columns;
        }
    }

    public static class ColumnDefinition {

        private String name;
        private int length;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }
    }
}
