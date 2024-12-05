package org.kkb.service;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class CsvBatchService {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job csvToDatabaseJob;

    public void processCsvFiles(List<File> files) {
        files.forEach(file -> {
            try {
                jobLauncher.run(csvToDatabaseJob, new JobParametersBuilder()
                        .addString("filePath", file.getAbsolutePath())
                        .toJobParameters());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
