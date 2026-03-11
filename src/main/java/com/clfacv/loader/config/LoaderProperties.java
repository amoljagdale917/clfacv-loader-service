package com.clfacv.loader.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app.loader")
public class LoaderProperties {

    private String inputDirectory;
    private String successDirectory;
    private String failedDirectory;
    private String inputCharset = "UTF-8";
    private int batchSize = 500;
    private List<FileDefinition> files = new ArrayList<FileDefinition>();

    @Getter
    @Setter
    public static class FileDefinition {

        private String fileName;
        private String dataSource;
        private String tableName;
        private List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
    }

    @Getter
    @Setter
    public static class ColumnDefinition {

        private String name;
        private int length;
    }
}
