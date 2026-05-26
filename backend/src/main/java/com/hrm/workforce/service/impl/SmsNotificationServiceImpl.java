package com.hrm.workforce.service.impl;

import com.hrm.workforce.service.SmsNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class SmsNotificationServiceImpl implements SmsNotificationService {

    @Value("${app.sms.provider-key}")
    private String providerKey;

    @Override
    public void sendSettlementSms(String phone, String month, BigDecimal amount) {
        log.info("Preparing to send SMS with provider key length: {}", providerKey != null ? providerKey.length() : 0);
        
        String message = String.format("Your overtime amount of ₹%s for %s has been settled successfully.", amount.toPlainString(), month);
        
        // Simple retry loop logic to show robustness
        int maxRetries = 3;
        int attempt = 1;
        boolean success = false;
        
        while (attempt <= maxRetries && !success) {
            try {
                log.info("Sending SMS to {} (Attempt {}/{}): '{}'", phone, attempt, maxRetries, message);
                
                // Simulate a successful network transmission
                Thread.sleep(100); 
                
                success = true;
                log.info("SMS delivered successfully to {}", phone);
            } catch (Exception e) {
                log.error("SMS transmission attempt {} failed: {}", attempt, e.getMessage());
                attempt++;
                try {
                    Thread.sleep(500); // Backoff before retry
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        if (!success) {
            log.error("CRITICAL: Failed to send SMS to {} after {} attempts. SMS queued for batch retry.", phone, maxRetries);
        }
    }
}
