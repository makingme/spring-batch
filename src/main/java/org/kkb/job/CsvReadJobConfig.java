package org.kkb.job;

import org.kkb.config.FileProperties;
import org.kkb.listener.CsvItemReadListener;
import org.kkb.listener.CsvItemWriteListener;
import org.kkb.model.KoreanFoodStore;
import org.kkb.tasklet.FileMoveTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
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
    @JobScope
    public Tasklet fileMoveToDoneTasklet(@Value("#{jobParameters['targetFile']}") String targetFile, @Value("#{stepExecution}") StepExecution stepExecution){
        ExecutionContext jobExecutionContext=stepExecution.getJobExecution().getExecutionContext();
        String step2Status = jobExecutionContext.getString("CsvToDatabaseStepStatus", "FAILED");
        return new FileMoveTasklet(fileProperties.getProcess()+targetFile, "SUCCESS".equals(step2Status)?fileProperties.getCompleted():fileProperties.getError());
    }


    @Bean
    public Step moveToDoneStep(JobRepository jobRepository, @Qualifier("fileMoveToDoneTasklet")Tasklet fileMoveToDoneTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("moveToDoneStep", jobRepository)
                .tasklet(fileMoveToDoneTasklet, transactionManager)
                .build();
    }


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
    public Step csvToDatabaseStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, FlatFileItemReader<KoreanFoodStore> csvReader,
                                  JdbcBatchItemWriter<KoreanFoodStore> jdbcWriter,
                                  @Value("#{jobParameters['targetFile']}") String targetFile) {
        int lastDotIndex = targetFile.lastIndexOf('.');
        String fileName = lastDotIndex == -1 ? targetFile : targetFile.substring(0, lastDotIndex)+"_"+System.currentTimeMillis();
        return new StepBuilder(fileName, jobRepository)
                .<KoreanFoodStore, KoreanFoodStore>chunk(chunkSize, transactionManager)
                .reader(csvReader)
                .writer(jdbcWriter)
                .listener(new CsvItemReadListener<KoreanFoodStore>(targetFile))
                .listener(new CsvItemWriteListener<KoreanFoodStore>(targetFile))
                .faultTolerant()
                .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .build();
    }

    @Bean
    @JobScope
    public Tasklet fileMoveToProcessTasklet(@Value("#{jobParameters['targetFile']}") String targetFile){
        return new FileMoveTasklet(fileProperties.getReady()+targetFile, fileProperties.getProcess());
    }


    @Bean
    public Step moveToProcessStep(JobRepository jobRepository, @Qualifier("fileMoveToProcessTasklet")Tasklet fileMoveToProccessTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("moveToProcessStep", jobRepository)
                .tasklet(fileMoveToProccessTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job csvToDatabaseJob(JobRepository jobRepository, @Qualifier("moveToProcessStep")Step moveToProcessStep, @Qualifier("csvToDatabaseStep")Step csvFileToDatabaseStep, @Qualifier("moveToDoneStep")Step moveToDoneStep) {
        return new JobBuilder("csvToDatabaseJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(moveToProcessStep)
                .next(csvFileToDatabaseStep)
                .next(moveToDoneStep)
                .build();
    }

}
