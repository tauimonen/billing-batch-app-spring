package example.billingapp;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

public class BillingDataProcessor implements ItemProcessor<BillingData, ReportingData> {

    @Value("${spring.cellular.pricing.data:0.01}")
    private float dataPricing;

    @Value("${spring.cellular.pricing.call:0.5}")
    private float callPricing;

    @Value("${spring.cellular.pricing.sms:0.1}")
    private float smsPricing;

    @Value("${spring.cellular.spending.threshold:150}")
    private float spendingThreshold;

    public ReportingData process(BillingData itemBillingData) {
        double billingTotal = (itemBillingData.dataUsage() * dataPricing) +
                (itemBillingData.callDuration() * callPricing) +
                (itemBillingData.smsCount() * smsPricing);
        if (billingTotal > spendingThreshold) {
            return null;
        }

        return new ReportingData(itemBillingData, billingTotal);
    }
}
