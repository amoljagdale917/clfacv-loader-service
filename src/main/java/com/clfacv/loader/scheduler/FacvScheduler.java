package com.clfacv.loader.scheduler;

import com.clfacv.loader.service.FileLoadingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FacvScheduler {

    private final FileLoadingService loadingService;

    @Scheduled(cron = "${app.loader.scheduler.cron}")
    public void loadFacvFiles() {
        log.info("File loader scheduler started");
        loadingService.processConfiguredFiles();
        log.info("File loader scheduler completed");
    }
}
