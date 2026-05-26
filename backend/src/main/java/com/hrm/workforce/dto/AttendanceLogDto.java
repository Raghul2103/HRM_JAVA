package com.hrm.workforce.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceLogDto {
    private Long id;
    private Long workerId;
    private String workerName;
    private String workerPhone;
    private String workerDesignation;
    private Long siteId;
    private String siteName;
    private String siteLocation;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private BigDecimal totalHours;
    private BigDecimal overtimeHours;
    private boolean flagged;
}
