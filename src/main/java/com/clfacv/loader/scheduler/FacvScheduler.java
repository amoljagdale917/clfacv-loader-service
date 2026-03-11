package com.clfacv.loader.scheduler;

import com.clfacv.loader.service.FileLoadingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FacvScheduler {

    private static final Logger log = LoggerFactory.getLogger(FacvScheduler.class);

    private final FileLoadingService loadingService;

    public FacvScheduler(FileLoadingService loadingService) {
        this.loadingService = loadingService;
    }

    @Scheduled(cron = "${app.scheduler.cron:0 0/5 * * * *}")
    public void loadFacvFiles() {
        log.info("File loader scheduler started");
        loadingService.processConfiguredFiles();
        log.info("File loader scheduler completed");
    }
}
