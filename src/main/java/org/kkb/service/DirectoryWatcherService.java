package org.kkb.service;

import org.kkb.module.DirectoryWatcher;
import org.kkb.module.FileSplitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class DirectoryWatcherService {
    private static final Logger log = LoggerFactory.getLogger(DirectoryWatcherService.class);

    @Value("${csv.read.job.chunk:1000}")
    private int chunkSize;

    @Value("${file.has.header:Y}")
    private String hasHeader;

    @Autowired
    private FileSplitter fileSplitter;

    @Autowired
    private CsvBatchService csvBatchService;

    public void startWatching(String directoryPath) {
        DirectoryWatcher watcher = new DirectoryWatcher(directoryPath);

        watcher.watchDirectory(filePath -> {
            try {
                // 분리된 파일 목록 생성
                List<File> splitFiles = fileSplitter.splitFile(filePath, chunkSize, "Y".equalsIgnoreCase(hasHeader));

                // 분리된 파일에 대해 병렬로 Job 실행
                List<CompletableFuture<String>> futures = splitFiles.stream()
                        .map(csvBatchService::executeBatchJob)
                        .toList();

                // 모든 Job 완료 대기
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenAccept(v -> System.out.println("All jobs completed."));

            } catch (IOException e) {
                e.printStackTrace();
            }
            return CompletableFuture.completedFuture("DONE");
        });
    }
}
