package info.doula;

import javax.sql.DataSource;

import info.doula.bean.Attempt;
import info.doula.listener.ChunkExecutionListener;
import info.doula.listener.JobCompletionNotificationListener;
import info.doula.listener.StepExecutionNotificationListener;
import info.doula.process.AttemptProcessor;
import info.doula.process.AttemptReader;
import info.doula.process.AttemptWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Mohammed Hossain Doula
 *
 * @hossaindoula | @itconquest
 * <p>
 * http://hossaindoula.com
 * <p>
 * https://github.com/hossaindoula
 */
@Configuration
@ComponentScan
@EnableAsync
@SpringBootApplication
@EnableBatchProcessing
public class SpringAsyncBatchJob extends DefaultBatchConfigurer {

    private final static Logger logger = LoggerFactory.getLogger(SpringAsyncBatchJob.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Value("${chunk-size}")
    private int chunkSize;

    @Value("${max-threads}")
    private int maxThreads;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource batchDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public AttemptReader processAttemptReader() {
        return new AttemptReader();
    }

    @Bean
    public AttemptProcessor processAttemptProcessor() {
        return new AttemptProcessor();
    }

    @Bean
    public AttemptWriter processAttemptWriter() {
        return new AttemptWriter();
    }

    @Bean
    public JobCompletionNotificationListener jobExecutionListener() {
        return new JobCompletionNotificationListener();
    }

    @Bean
    public StepExecutionNotificationListener stepExecutionListener() {
        return new StepExecutionNotificationListener();
    }

    @Bean
    public ChunkExecutionListener chunkListener() {
        return new ChunkExecutionListener();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(maxThreads);
        return taskExecutor;
    }

    @Bean
    public Job processAttemptJob() {
        return jobBuilderFactory.get("process-attempt-job")
                .incrementer(new RunIdIncrementer())
                .listener(jobExecutionListener())
                .flow(step()).end().build();
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("step").<Attempt, Attempt>chunk(chunkSize)
                .reader(processAttemptReader())
                .processor(processAttemptProcessor())
                .writer(processAttemptWriter())
                .taskExecutor(taskExecutor())
                .listener(stepExecutionListener())
                .listener(chunkListener())
                .throttleLimit(maxThreads).build();
    }

    public static void main(String[] args) {
        //long time = System.currentTimeMillis();
        SpringApplication.run(SpringAsyncBatchJob.class, args);
        //time = System.currentTimeMillis() - time;
        //logger.info("Runtime: {} seconds.", ((double)time/1000));
    }
}
