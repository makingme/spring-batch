package org.kkb.job;

import org.kkb.model.FoodStore;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.File;

@Configuration
@EnableBatchProcessing
public class CsvReadJobConfig {
    @Autowired
    private DataSource dataSource;

    @Bean
    @JobScope
    public FlatFileItemReader<FoodStore> csvReader(@Value("#{jobParameters['filePath']}") String filePath) {
        return new FlatFileItemReaderBuilder<FoodStore>()
                .name("csvReader")
                .resource(new FileSystemResource(filePath))
                .linesToSkip(1)
                .delimited()
                .names("seq", "storeName", "storeNo", "storeCall")
                .targetType(FoodStore.class)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<FoodStore> jdbcWriter() {
        return new JdbcBatchItemWriterBuilder<FoodStore>()
                .dataSource(dataSource)
                .sql("INSERT INTO FoodStore (SEQ, STORE_NAME, STORE_NO, STORE_CALL) VALUES (:seq, :storeName, :storeNo, :storeCall)")
                .beanMapped()
                .build();
    }

    @Bean
    @JobScope
    public Step csvFileToDatabaseStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, FlatFileItemReader<FoodStore> csvReader,
                                      JdbcBatchItemWriter<FoodStore> jdbcWriter,
                                      @Value("#{jobParameters['filePath']}") String filePath) {
        String fileName = filePath != null ? new File(filePath).getName() : "defaultStep";
        return new StepBuilder(fileName, jobRepository)
                .<FoodStore, FoodStore>chunk(5, transactionManager)
                .reader(csvReader)
                .writer(jdbcWriter)
                .faultTolerant()
                .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .build();
    }

    @Bean
    public Job csvToDatabaseJob(JobRepository jobRepository, Step csvFileToDatabaseStep) {
        return new JobBuilder("csvToDatabaseJob", jobRepository)
                .start(csvFileToDatabaseStep)
                .build();
    }
}
