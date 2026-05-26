package com.hrm.workforce.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "overtime_entries", indexes = {
    @Index(name = "idx_overtime_worker_month", columnList = "worker_id, month_string")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendance_log_id", nullable = false)
    private AttendanceLog attendance;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "overtime_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal overtimeHours;

    @Column(name = "overtime_rate_applied", nullable = false, precision = 10, scale = 2)
    private BigDecimal overtimeRateApplied;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false, length = 50)
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;

    @Column(name = "month_string", nullable = false, length = 7) // YYYY-MM
    private String monthString;
}
