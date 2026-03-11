package com.clfacv.loader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClfacvLoaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClfacvLoaderApplication.class, args);
    }
}
