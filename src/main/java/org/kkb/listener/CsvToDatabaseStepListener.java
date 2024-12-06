package org.kkb.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;

public class CsvToDatabaseStepListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(CsvToDatabaseStepListener.class);

    private final String targetFileName;

    public CsvToDatabaseStepListener(String targetFileName) {
        this.targetFileName = targetFileName;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("{} File Read & Write Start", targetFileName);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("{} File Read & Write End", targetFileName);
        ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
        if (stepExecution.getFailureExceptions().isEmpty()&& stepExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
            jobExecutionContext.putString("CsvToDatabaseStepStatus", "SUCCESS");
        } else {
            jobExecutionContext.putString("CsvToDatabaseStepStatus", "FAILED");
        }
        return stepExecution.getExitStatus();
    }
}
