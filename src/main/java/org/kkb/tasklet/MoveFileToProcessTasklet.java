package org.kkb.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class MoveFileToProcessTasklet implements Tasklet {

    public MoveFileToProcessTasklet(String sourceFilePath, String destinationDirPath) {
        this.sourceFilePath = sourceFilePath;
        this.destinationDirPath = destinationDirPath;
    }

    private final String sourceFilePath;
    private final String destinationDirPath;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        File sourceFile = new File(sourceFilePath);
        if (!sourceFile.exists()) {
            throw new IllegalArgumentException("Source file does not exist: " + sourceFilePath);
        }

        File destinationDir = new File(destinationDirPath);
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }

        Path targetPath = destinationDir.toPath().resolve(sourceFile.getName());
        Files.move(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return RepeatStatus.FINISHED;
    }
}
