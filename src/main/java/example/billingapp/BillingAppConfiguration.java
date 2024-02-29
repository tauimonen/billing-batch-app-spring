package example.billingapp;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.support.JdbcTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BillingAppConfiguration {

    @Bean
    public Job job(JobRepository jobRepository, Step step1) {
        return new JobBuilder("BillingJob", jobRepository)
                .start(step1)
                .build();
    }
    @Bean
    public Step step1(JobRepository jobRepository, JdbcTransactionManager transactionManager) {
        return new StepBuilder("filePreparation", jobRepository)
                .tasklet(new FilePreparationTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step step2(
            JobRepository jobRepository, JdbcTransactionManager transactionManager,
            ItemReader<BillingData> billingDataFileReader, ItemWriter<BillingData> billingDataTableWriter) {
        return new StepBuilder("fileIngestion", jobRepository)
                .<BillingData, BillingData>chunk(100, transactionManager)
                .reader(billingDataFileReader)
                .writer(billingDataTableWriter)
                .build();
    }

    @Bean
    public FlatFileItemReader<BillingData> billingDataReader() {
        return new FlatFileItemReaderBuilder<BillingData>()
                .name("billingDataReader")
                .resource(new FileSystemResource("stage/billing-data.csv"))
                .delimited()
                .names("dataYear", "dataMonth", "accountId", "phoneNumber", "dataUsage", "callDuration", "smsCount")
                .targetType(BillingData.class)
                .build();
    }

    @Bean
    JdbcBatchItemWriter<BillingData> billingDataTableWriter(DataSource dataSource) {
        String sql =  "insert into billing_data (data_year, data_month, account_id, phone_number, data_usage, call_duration, sms_count) " +
                "values (:dataYear, :dataMonth, :accountId, :phoneNumber, :dataUsage, :callDuration, :smsCount)";

        return new JdbcBatchItemWriterBuilder<BillingData>()
                .dataSource(dataSource)
                .sql(sql)
                .beanMapped()
                .build();
    }
}
