package com.balram.spring.batch;

import com.balram.spring.batch.domain.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StopWatch;

import javax.sql.DataSource;
@Configuration
@Slf4j
public class AsyncBatchConfig {

    @Autowired
    PlatformTransactionManager platformTransactionManager;

    @Autowired
    DataSource dataSource;

    @Bean
    @StepScope
    public FlatFileItemReader<Transaction> fileTransactionReader(
            @Value("#{jobParameters['inputFileName']}") String resource) {

        return new FlatFileItemReaderBuilder<Transaction>()
                .saveState(false)
                .resource(new FileSystemResource(resource))
                .delimited()
                .names(new String[] {"account", "amount", "timestamp"})
                .fieldSetMapper(fieldSet -> {
                    Transaction transaction = new Transaction();
                    transaction.setAccount(fieldSet.readString("account"));
                    transaction.setAmount(fieldSet.readBigDecimal("amount"));
                    transaction.setTimestamp(fieldSet.readDate("timestamp", "yyyy-MM-dd HH:mm:ss"));
                    return transaction;
                })
                .build();
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Transaction> writer() {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("INSERT INTO TRANSACTION (ACCOUNT, AMOUNT, TIMESTAMP) VALUES (:account, :amount, :timestamp)")
                .build();
    }

    @Bean
    public ItemProcessor<Transaction, Transaction> processor() {
        return (transaction) -> {
            Thread.sleep(5);
            return transaction;
        };
    }

    @Bean
    public AsyncItemProcessor<Transaction, Transaction> asyncItemProcessor() {
        AsyncItemProcessor<Transaction, Transaction> processor = new AsyncItemProcessor<>();

        processor.setDelegate(processor());
        processor.setTaskExecutor(new SimpleAsyncTaskExecutor());
        log.info("Async Process ");
        return processor;
    }


    @Bean
    public JobLauncher asyncJobLauncher(JobRepository jobRepository){
        TaskExecutorJobLauncher taskExecutorJobLauncher = new TaskExecutorJobLauncher();
        taskExecutorJobLauncher.setJobRepository(jobRepository);
        taskExecutorJobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return taskExecutorJobLauncher;
    }

    @Bean
    public AsyncItemWriter<Transaction> asyncItemWriter() {
        AsyncItemWriter<Transaction> writer = new AsyncItemWriter<>();

        writer.setDelegate(writer());
        log.info("Async asyncItemWriter ");
        return writer;
    }

    @Bean
    public Job asyncBatchJob(JobRepository jobRepository) {
        return new JobBuilder("asyncBatchJob", jobRepository)
                .start(asyncBatchStep(jobRepository, platformTransactionManager))
                .listener(new ExecutionTimeJobListener())
                .build();
    }

    @Bean
    public Job singleThreadedJob(JobRepository jobRepository) {
        return new JobBuilder("singleThreadedJob", jobRepository)
                .start(normalStep(jobRepository, platformTransactionManager))
                .listener(new ExecutionTimeJobListener())
                .build();
    }

    @Bean
    public Step asyncBatchStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("asyncBatchStep", jobRepository)
                .<Transaction, Transaction>chunk(1000, platformTransactionManager)
                .reader(fileTransactionReader(null))
                .processor((ItemProcessor) asyncItemProcessor())
                .writer(asyncItemWriter())
                .build();
    }

    @Bean
    public Step normalStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("normalStep", jobRepository)
                .<Transaction, Transaction>chunk(1000, platformTransactionManager)
                .reader(fileTransactionReader(null))
                .processor(processor())
                .writer(writer())
                .build();
    }

    class ExecutionTimeJobListener implements JobExecutionListener {
        private StopWatch stopWatch = new StopWatch();

        @Override
        public void beforeJob(JobExecution jobExecution) {
            stopWatch.start();
        }

        @Override
        public void afterJob(JobExecution jobExecution) {
            stopWatch.stop();
            log.info("Job took " + stopWatch.getTotalTimeSeconds() + "s");
        }
    }
}

