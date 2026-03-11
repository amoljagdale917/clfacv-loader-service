package com.clfacv.loader.scheduler;

import com.clfacv.loader.service.FacvFileProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FacvScheduler {

    private static final Logger log = LoggerFactory.getLogger(FacvScheduler.class);

    private final FacvFileProcessingService processingService;

    public FacvScheduler(FacvFileProcessingService processingService) {
        this.processingService = processingService;
    }

    @Scheduled(cron = "${app.scheduler.cron:0 0/5 * * * *}")
    public void loadFacvFiles() {
        log.info("FACV scheduler started");
        processingService.processConfiguredFiles();
        log.info("FACV scheduler completed");
    }
}
