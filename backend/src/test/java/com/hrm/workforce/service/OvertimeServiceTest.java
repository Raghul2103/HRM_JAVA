package com.hrm.workforce.service;

import com.hrm.workforce.dto.OvertimeSummaryDto;
import com.hrm.workforce.entity.*;
import com.hrm.workforce.event.OvertimeSettledEvent;
import com.hrm.workforce.exception.BadRequestException;
import com.hrm.workforce.repository.OvertimeRepository;
import com.hrm.workforce.repository.WorkerRepository;
import com.hrm.workforce.service.impl.OvertimeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OvertimeServiceTest {

    @Mock
    private OvertimeRepository overtimeRepository;
    @Mock
    private WorkerRepository workerRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OvertimeServiceImpl overtimeService;

    private Worker worker;
    private OvertimeEntry overtimeEntry;

    @BeforeEach
    void setUp() {
        worker = Worker.builder()
                .id(1L)
                .name("Rajesh Kumar")
                .phone("9876543210")
                .designation(Designation.MASON)
                .dailyWageRate(new BigDecimal("500.00")) // Standard hourly rate = 500 / 8 = 62.5
                .active(true)
                .build();

        AttendanceLog attendanceLog = AttendanceLog.builder()
                .id(10L)
                .worker(worker)
                .build();

        overtimeEntry = OvertimeEntry.builder()
                .id(20L)
                .worker(worker)
                .attendance(attendanceLog)
                .date(LocalDate.now().minusMonths(1))
                .overtimeHours(new BigDecimal("2.00"))
                .overtimeRateApplied(new BigDecimal("62.50"))
                .amount(new BigDecimal("187.50")) // 2 * 62.5 * 1.5
                .settlementStatus(SettlementStatus.PENDING)
                .monthString(LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM")))
                .build();
    }

    @Test
    void testGetOvertimeSummary() {
        String month = overtimeEntry.getMonthString();
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(overtimeRepository.findByWorkerIdAndMonthString(1L, month))
                .thenReturn(Collections.singletonList(overtimeEntry));

        OvertimeSummaryDto summary = overtimeService.getOvertimeSummary(1L, month);

        assertNotNull(summary);
        assertEquals(SettlementStatus.PENDING, summary.getSettlementStatus());
        assertEquals(new BigDecimal("2.00"), summary.getTotalOvertimeHours());
        assertEquals(new BigDecimal("187.50"), summary.getTotalPayoutAmount());
        assertEquals(1, summary.getBreakdown().size());
    }

    @Test
    void testSettleOvertimeSuccess() {
        String month = overtimeEntry.getMonthString();
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(overtimeRepository.findByWorkerIdAndMonthStringAndSettlementStatus(1L, month, SettlementStatus.PENDING))
                .thenReturn(Collections.singletonList(overtimeEntry));

        BigDecimal settledAmount = overtimeService.settleOvertime(1L, month, new BigDecimal("400.00")); // gov limit 400, worker wage 500

        assertEquals(new BigDecimal("187.50"), settledAmount);
        assertEquals(SettlementStatus.SETTLED, overtimeEntry.getSettlementStatus());
        verify(overtimeRepository, times(1)).save(overtimeEntry);
        verify(eventPublisher, times(1)).publishEvent(any(OvertimeSettledEvent.class));
    }

    @Test
    void testSettleOvertimeCurrentMonthThrowsException() {
        // Can't settle current month
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));

        assertThrows(BadRequestException.class, () -> 
                overtimeService.settleOvertime(1L, currentMonth, new BigDecimal("400.00")));
    }

    @Test
    void testSettleOvertimeLowWageRateThrowsException() {
        String month = overtimeEntry.getMonthString();
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));

        // Gov wage standard is 600.00, worker daily wage is 500.00
        assertThrows(BadRequestException.class, () -> 
                overtimeService.settleOvertime(1L, month, new BigDecimal("600.00")));
    }
}
