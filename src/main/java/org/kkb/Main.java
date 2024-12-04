package org.kkb;

import org.kkb.config.AppConfig;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        JobLauncher jobLauncher = context.getBean(JobLauncher.class);
        Job csvToDatabaseJob = context.getBean("csvToDatabaseJob", Job.class);

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("filePath", "C:/Users/uracle/git_project/SpringBatch/spring-batch/Data/foodstore1.csv")
                    .toJobParameters();
            JobExecution execution = jobLauncher.run(csvToDatabaseJob, jobParameters);
            System.out.println("Job Execution Status: " + execution.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            context.close();
        }
    }
}