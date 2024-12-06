package org.kkb.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class MoveFileAfterProcessTasklet implements Tasklet {

    public MoveFileAfterProcessTasklet(String sourceFilePath, String completedPath, String errorPath) {
        this.sourceFilePath = sourceFilePath;
        this.completedPath = completedPath;
        this.errorPath = errorPath;
    }

    private final String sourceFilePath;
    private final String completedPath;
    private final String errorPath;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
        ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
        String step2Status = jobExecutionContext.getString("CsvToDatabaseStepStatus", "FAILED");
        File sourceFile = new File(sourceFilePath);
        if (!sourceFile.exists()) {
            throw new IllegalArgumentException("Source file does not exist: " + sourceFilePath);
        }
        String destinationPath = "SUCCESS".equalsIgnoreCase(step2Status)?completedPath:errorPath;
        File destinationDir = new File(destinationPath);
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }

        Path targetPath = destinationDir.toPath().resolve(sourceFile.getName());
        Files.move(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return RepeatStatus.FINISHED;
    }
}
