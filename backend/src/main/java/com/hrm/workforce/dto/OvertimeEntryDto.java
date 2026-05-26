package com.hrm.workforce.dto;

import com.hrm.workforce.entity.SettlementStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeEntryDto {
    private Long id;
    private Long workerId;
    private String workerName;
    private Long attendanceId;
    private LocalDate date;
    private BigDecimal overtimeHours;
    private BigDecimal overtimeRateApplied;
    private BigDecimal amount;
    private SettlementStatus settlementStatus;
    private String monthString;
}
