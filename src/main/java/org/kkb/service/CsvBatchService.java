package org.kkb.service;

import org.kkb.config.FileProperties;
import org.kkb.config.QueryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CsvBatchService {
    private static final Logger log = LoggerFactory.getLogger(CsvBatchService.class);

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job csvToDatabaseJob;

    @Autowired
    private FileProperties fileProperties;
    @Autowired
    private QueryProperties queryProperties;

    public CompletableFuture<String> executeBatchJob(File file) {
        JobParameters params = new JobParametersBuilder()
                .addString("targetFile", file.getName())
                .addString("insertQuery", queryProperties.getInsertQuery())
                .addLong("timestamp", System.nanoTime())
                .addString("jobId", UUID.randomUUID().toString())
                .toJobParameters();
        return CompletableFuture.supplyAsync(() -> {
            try {
                JobExecution execution = jobLauncher.run(csvToDatabaseJob, params);
                return execution.getExitStatus().getExitCode();
            } catch (Exception e) {
                log.error("{} 파일 처리 중 에러 - {}", file.getName(), e.getMessage());
                log.error("에러 상세:", e);
                return "FAILED";
            }
        }, executorService).handle((s, ex) -> {
            if(ex != null || "FAILED".equals(s)) {
                Path path = file.toPath();
                Path destinationDir = Paths.get(fileProperties.getError());
                Path destination =  destinationDir.resolve(path.getFileName());
                try {
                    Files.move(path, destination, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    log.error("{} 파일 ERROR 폴더 이동  실패 - {}", file.getName(), e.getMessage());
                    log.error("에러 상세 : ", e);
                }
            }
            return s;
        });
    }
}
