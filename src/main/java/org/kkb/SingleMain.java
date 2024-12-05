package org.kkb;

import org.kkb.config.AppConfig;
import org.kkb.config.FileProperties;
import org.kkb.config.QueryProperties;
import org.kkb.util.AESEncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.nio.file.Files;
import java.nio.file.Paths;


public class SingleMain {

    private static final Logger log = LoggerFactory.getLogger(SingleMain.class);

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        JobLauncher jobLauncher = context.getBean(JobLauncher.class);
        Job csvToDatabaseJob = context.getBean("csvToDatabaseJob", Job.class);
        FileProperties fileProperties = context.getBean("fileProperties", FileProperties.class);
        QueryProperties queryProperties = context.getBean("queryProperties", QueryProperties.class);
        final String targetFile = fileProperties.getSingleTarget();
        if(targetFile == null || targetFile.isEmpty()){
            log.error("싱글 배치 설정 이상 - 설정 [file.single.target] 누락");
            System.exit(0);
        }
        // 싱글은 Ready 경로부터 시작
        String readyPath = fileProperties.getReady();
        if(!Files.exists(Paths.get(readyPath+targetFile))){
            log.error("싱글 배치 파일 이상 - {} 경로에 {} 처리 파일이 없음", readyPath, targetFile);
            System.exit(0);
        }
        final String insertQuery = queryProperties.getInsertQuery();
        if(insertQuery == null || insertQuery.isEmpty()){
            log.error("싱글 배치 설정 이상 - 설정 [mysql.insert.query] 누락");
            System.exit(0);
        }
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("targetFile", targetFile)
                    .addString("insertQuery", insertQuery)
                    .addLong("startTime", System.currentTimeMillis())
                    .toJobParameters();
            JobExecution execution = jobLauncher.run(csvToDatabaseJob, jobParameters);
            log.info("Job Execution Status: {}" , execution.getStatus());
        } catch (Exception e) {
            log.error("싱글 배치 처리 중 에러 발생 - {}", e.getMessage());
            log.error("에러상세:", e);
        } finally {
            context.close();
        }
    }
}