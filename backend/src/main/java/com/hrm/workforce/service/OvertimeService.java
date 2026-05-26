package com.hrm.workforce.service;

import com.hrm.workforce.dto.OvertimeSummaryDto;

import java.math.BigDecimal;

public interface OvertimeService {
    OvertimeSummaryDto getOvertimeSummary(Long workerId, String month);
    BigDecimal settleOvertime(Long workerId, String month, BigDecimal govWageStandard);
}
