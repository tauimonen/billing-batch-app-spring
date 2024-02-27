package example.billingapp;

import org.springframework.batch.core.Job;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BillingAppConfiguration {

    @Bean
    public Job job() {
        return new BillingJob();
    }
}
