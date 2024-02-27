package example.billingapp;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;

public class BillingJob implements Job {
    @Override
    public String getName() {
        return "BillingJob";
    }

    @Override
    public void execute(JobExecution execution) {
        System.out.println("Processing billing job");
    }
}
