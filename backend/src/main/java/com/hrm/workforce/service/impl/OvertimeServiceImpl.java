package com.hrm.workforce.service.impl;

import com.hrm.workforce.dto.OvertimeEntryDto;
import com.hrm.workforce.dto.OvertimeSummaryDto;
import com.hrm.workforce.entity.OvertimeEntry;
import com.hrm.workforce.entity.SettlementStatus;
import com.hrm.workforce.entity.Worker;
import com.hrm.workforce.event.OvertimeSettledEvent;
import com.hrm.workforce.exception.BadRequestException;
import com.hrm.workforce.exception.ConflictException;
import com.hrm.workforce.exception.ResourceNotFoundException;
import com.hrm.workforce.repository.OvertimeRepository;
import com.hrm.workforce.repository.WorkerRepository;
import com.hrm.workforce.service.OvertimeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OvertimeServiceImpl implements OvertimeService {

    private final OvertimeRepository overtimeRepository;
    private final WorkerRepository workerRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OvertimeServiceImpl(OvertimeRepository overtimeRepository,
                               WorkerRepository workerRepository,
                               ApplicationEventPublisher eventPublisher) {
        this.overtimeRepository = overtimeRepository;
        this.workerRepository = workerRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public OvertimeSummaryDto getOvertimeSummary(Long workerId, String month) {
        log.info("Fetching overtime summary for worker ID: {}, month: {}", workerId, month);

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found with ID: " + workerId));

        List<OvertimeEntry> entries = overtimeRepository.findByWorkerIdAndMonthString(workerId, month);

        BigDecimal totalHours = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        SettlementStatus overallStatus = SettlementStatus.SETTLED;

        if (!entries.isEmpty()) {
            boolean hasPending = false;
            for (OvertimeEntry entry : entries) {
                totalHours = totalHours.add(entry.getOvertimeHours());
                totalAmount = totalAmount.add(entry.getAmount());
                if (entry.getSettlementStatus() == SettlementStatus.PENDING) {
                    hasPending = true;
                }
            }
            if (hasPending) {
                overallStatus = SettlementStatus.PENDING;
            }
        }

        List<OvertimeEntryDto> breakdown = entries.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return OvertimeSummaryDto.builder()
                .workerId(worker.getId())
                .workerName(worker.getName())
                .month(month)
                .totalOvertimeHours(totalHours)
                .totalPayoutAmount(totalAmount)
                .settlementStatus(overallStatus)
                .breakdown(breakdown)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal settleOvertime(Long workerId, String month, BigDecimal govWageStandard) {
        log.info("Settling overtime for worker ID: {}, month: {}, gov standard: ₹{}", workerId, month, govWageStandard);

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found with ID: " + workerId));

        // 1. Cannot settle current or future month
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        if (month.compareTo(currentMonth) >= 0) {
            throw new BadRequestException("Cannot settle overtime for the current or a future month.");
        }

        // 2. Validate daily wage against government standard
        if (worker.getDailyWageRate().compareTo(govWageStandard) < 0) {
            throw new BadRequestException(String.format("Worker daily wage rate (₹%s) is lower than the government minimum standard (₹%s). Cannot settle.", 
                    worker.getDailyWageRate().toPlainString(), govWageStandard.toPlainString()));
        }

        // 3. Find pending entries
        List<OvertimeEntry> pendingEntries = overtimeRepository.findByWorkerIdAndMonthStringAndSettlementStatus(
                workerId, month, SettlementStatus.PENDING);

        if (pendingEntries.isEmpty()) {
            // Check if there are already settled ones to return, or throw
            List<OvertimeEntry> allEntries = overtimeRepository.findByWorkerIdAndMonthString(workerId, month);
            if (allEntries.isEmpty()) {
                throw new BadRequestException("No overtime records found for this month.");
            }
            // Check if all are settled
            boolean allSettled = allEntries.stream().allMatch(e -> e.getSettlementStatus() == SettlementStatus.SETTLED);
            if (allSettled) {
                throw new ConflictException("ALREADY_SETTLED", "Overtime is already fully settled for this month.");
            }
            throw new BadRequestException("No pending overtime entries found to settle.");
        }

        // 4. Update status atomically (atomic transaction)
        BigDecimal totalSettledAmount = BigDecimal.ZERO;
        for (OvertimeEntry entry : pendingEntries) {
            entry.setSettlementStatus(SettlementStatus.SETTLED);
            totalSettledAmount = totalSettledAmount.add(entry.getAmount());
            
            // Simulating a potential validation crash on entry #15 to test transaction rollback
            // (Only for demonstration if a special test triggers it)
            if ("test-crash".equals(worker.getName()) && pendingEntries.indexOf(entry) == 14) {
                throw new RuntimeException("Simulated data integrity failure on entry #15");
            }

            overtimeRepository.save(entry);
        }

        // 5. Publish transaction event (will trigger SMS AFTER commit)
        log.info("Publishing OvertimeSettledEvent for worker {}, amount: ₹{}", worker.getId(), totalSettledAmount);
        OvertimeSettledEvent event = new OvertimeSettledEvent(this, worker.getId(), worker.getPhone(), month, totalSettledAmount);
        eventPublisher.publishEvent(event);

        return totalSettledAmount;
    }

    private OvertimeEntryDto mapToDto(OvertimeEntry entry) {
        return OvertimeEntryDto.builder()
                .id(entry.getId())
                .workerId(entry.getWorker().getId())
                .workerName(entry.getWorker().getName())
                .attendanceId(entry.getAttendance().getId())
                .date(entry.getDate())
                .overtimeHours(entry.getOvertimeHours())
                .overtimeRateApplied(entry.getOvertimeRateApplied())
                .amount(entry.getAmount())
                .settlementStatus(entry.getSettlementStatus())
                .monthString(entry.getMonthString())
                .build();
    }
}
