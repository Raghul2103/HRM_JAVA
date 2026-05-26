package com.hrm.workforce.event;

import com.hrm.workforce.service.SmsNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class OvertimeSettledEventListener {

    private final SmsNotificationService smsNotificationService;

    public OvertimeSettledEventListener(SmsNotificationService smsNotificationService) {
        this.smsNotificationService = smsNotificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async // Run asynchronously to avoid holding up the HTTP thread response
    public void handleOvertimeSettled(OvertimeSettledEvent event) {
        log.info("Transaction COMMITTED. Dispatching SMS event for worker: {}", event.getWorkerId());
        try {
            smsNotificationService.sendSettlementSms(event.getPhone(), event.getMonth(), event.getTotalAmount());
        } catch (Exception e) {
            log.error("Error occurred in AFTER_COMMIT SMS listener: {}", e.getMessage(), e);
        }
    }
}
