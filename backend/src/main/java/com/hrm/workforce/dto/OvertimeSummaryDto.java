package com.hrm.workforce.dto;

import com.hrm.workforce.entity.SettlementStatus;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeSummaryDto {
    private Long workerId;
    private String workerName;
    private String month;
    private BigDecimal totalOvertimeHours;
    private BigDecimal totalPayoutAmount;
    private SettlementStatus settlementStatus;
    private List<OvertimeEntryDto> breakdown;
}
