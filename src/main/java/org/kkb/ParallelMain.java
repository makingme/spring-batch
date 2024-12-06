package org.kkb;

import org.kkb.config.AppConfig;
import org.kkb.config.FileProperties;
import org.kkb.service.DirectoryWatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelMain {
    private static final Logger log = LoggerFactory.getLogger(ParallelMain.class);
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        DirectoryWatcherService watcherService = context.getBean(DirectoryWatcherService.class);
        FileProperties fileProperties = context.getBean("fileProperties", FileProperties.class);
        // ExecutorService 사용
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(() -> watcherService.startWatching(fileProperties.getReceivePath()));

        try {
            // 메인 스레드 유지
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        }catch (InterruptedException e){
            log.error("Interrupted Signal");
        }
    }
}
