package com.hrm.workforce.service;

import com.hrm.workforce.entity.Designation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class GovernmentWageService {

    private final RestTemplate restTemplate;

    @Value("${app.external-api.gov-wage-url}")
    private String govWageUrl;

    public GovernmentWageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BigDecimal getMinimumWageRate(Designation designation) {
        log.info("Fetching minimum wage rate for {} from government API: {}", designation, govWageUrl);
        
        // Simulating slow network I/O
        try {
            Thread.sleep(1000); // 1-second latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread sleep interrupted during API mock", e);
        }

        // Mock call attempt via restTemplate
        try {
            // In a real application, this would fetch from the external URL:
            // Map response = restTemplate.getForObject(govWageUrl + "?designation=" + designation, Map.class);
            // return new BigDecimal(response.get("minimumRate").toString());
            
            // We use a local map lookup to act as the successful API result:
            Map<Designation, BigDecimal> rates = new HashMap<>();
            rates.put(Designation.MASON, new BigDecimal("450.00"));
            rates.put(Designation.ELECTRICIAN, new BigDecimal("400.00"));
            rates.put(Designation.PLUMBER, new BigDecimal("380.00"));
            rates.put(Designation.SUPERVISOR, new BigDecimal("600.00"));
            rates.put(Designation.HELPER, new BigDecimal("250.00"));
            
            BigDecimal rate = rates.getOrDefault(designation, new BigDecimal("200.00"));
            log.info("Minimum wage rate returned for {}: {}", designation, rate);
            return rate;
        } catch (Exception e) {
            log.error("Failed to query external government wage API. Using fallback rates.", e);
            return new BigDecimal("200.00");
        }
    }
}
