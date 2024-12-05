package org.kkb.job;

import org.kkb.config.FileProperties;
import org.kkb.model.KoreanFoodStore;
import org.kkb.tasklet.FileMoveTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class CsvReadJobConfig {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private FileProperties fileProperties;

    @Value("${csv.read.job.chunk:1000}")
    private int chunkSize;

    @Bean
    @StepScope
    public FlatFileItemReader<KoreanFoodStore> csvReader(@Value("#{jobParameters['targetFile']}") String targetFile) {
        String headerSkip = fileProperties.getHeaderSkip();
        String[] headers = fileProperties.getFileHeader();
        FlatFileItemReader<KoreanFoodStore> reader = new FlatFileItemReader<>();
        reader.setName("csvReader");
        reader.setResource(new FileSystemResource(fileProperties.getProcess()+targetFile));
        if("Y".equalsIgnoreCase(headerSkip))reader.setLinesToSkip(1);
        reader.setLineMapper(new DefaultLineMapper<KoreanFoodStore>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(headers);
                setDelimiter(",");
                setQuoteCharacter('"');
                setStrict(false);
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<KoreanFoodStore>() {{
                setTargetType(KoreanFoodStore.class);
            }});
        }});

       return reader;
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<KoreanFoodStore> jdbcWriter(@Value("#{jobParameters['insertQuery']}") String insertQuery) {
        return new JdbcBatchItemWriterBuilder<KoreanFoodStore>()
                .dataSource(dataSource)
                .sql(insertQuery)
                .beanMapped()
                .build();
    }
    
    @Bean
    @JobScope
    public Step csvFileToDatabaseStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, FlatFileItemReader<KoreanFoodStore> csvReader,
                                      JdbcBatchItemWriter<KoreanFoodStore> jdbcWriter,
                                      @Value("#{jobParameters['targetFile']}") String targetFile) {
        int lastDotIndex = targetFile.lastIndexOf('.');
        String fileName = lastDotIndex == -1 ? targetFile : targetFile.substring(0, lastDotIndex)+"_"+System.currentTimeMillis();
        return new StepBuilder(fileName, jobRepository)
                .<KoreanFoodStore, KoreanFoodStore>chunk(chunkSize, transactionManager)
                .reader(csvReader)
                .writer(jdbcWriter)
                .faultTolerant()
                .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .build();
    }

    @Bean
    @JobScope
    public Tasklet fileMoveToProccessTasklet(@Value("#{jobParameters['targetFile']}") String targetFile){
        return new FileMoveTasklet(fileProperties.getReady()+targetFile, fileProperties.getProcess());
    }


    @Bean
    public Step moveToProcessStep(JobRepository jobRepository, Tasklet fileMoveToProccessTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("moveToProcessStep", jobRepository)
                .tasklet(fileMoveToProccessTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job csvToDatabaseJob(JobRepository jobRepository, @Qualifier("moveToProcessStep")Step moveToProcessStep, @Qualifier("csvFileToDatabaseStep")Step csvFileToDatabaseStep) {
        return new JobBuilder("csvToDatabaseJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(moveToProcessStep)
                .next(csvFileToDatabaseStep)
                .build();
    }

}
