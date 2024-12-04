package org.kkb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class DirectoryWatcher {
    private static final Logger log = LoggerFactory.getLogger(DirectoryWatcher.class);
    private final Path directoryPath;
    private boolean isRun = false;
    public DirectoryWatcher(String directory) {
        this.directoryPath = Paths.get(directory);
    }

    public void watchDirectory(Function<String, CompletableFuture<String>> onFileDetected) {
        isRun = true;
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            directoryPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            System.out.println("Watching directory: " + directoryPath);

            while (isRun) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path filePath = directoryPath.resolve((Path) event.context());
                        System.out.println("File detected: " + filePath);

                        // Asynchronously handle the detected file
                        onFileDetected.apply(filePath.toString())
                                .thenAccept(result -> {
                                    System.out.println("Job Result: " + result);

                                    // Decide on reprocessing or other actions based on the result
                                    if ("RETRY".equals(result)) {
                                        System.out.println("Retrying job for file: " + filePath);
                                    } else {
                                        System.out.println("Processing completed for file: " + filePath);
                                    }
                                });
                    }
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            isRun = false;
        }
    }
}
