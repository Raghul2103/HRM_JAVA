package com.hrm.workforce.service;

import java.math.BigDecimal;

public interface SmsNotificationService {
    void sendSettlementSms(String phone, String month, BigDecimal amount);
}
