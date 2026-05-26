package com.hrm.workforce.service.impl;

import com.hrm.workforce.cache.ActiveWorkerCache;
import com.hrm.workforce.cache.ActiveWorkerEntry;
import com.hrm.workforce.dto.AttendanceLogDto;
import com.hrm.workforce.dto.ClockInRequest;
import com.hrm.workforce.dto.ClockOutRequest;
import com.hrm.workforce.dto.PageResponse;
import com.hrm.workforce.entity.*;
import com.hrm.workforce.exception.BadRequestException;
import com.hrm.workforce.exception.ConflictException;
import com.hrm.workforce.exception.ResourceNotFoundException;
import com.hrm.workforce.repository.AttendanceRepository;
import com.hrm.workforce.repository.OvertimeRepository;
import com.hrm.workforce.repository.SiteRepository;
import com.hrm.workforce.repository.WorkerRepository;
import com.hrm.workforce.service.AttendanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final WorkerRepository workerRepository;
    private final SiteRepository siteRepository;
    private final OvertimeRepository overtimeRepository;
    private final ActiveWorkerCache activeWorkerCache;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                 WorkerRepository workerRepository,
                                 SiteRepository siteRepository,
                                 OvertimeRepository overtimeRepository,
                                 ActiveWorkerCache activeWorkerCache) {
        this.attendanceRepository = attendanceRepository;
        this.workerRepository = workerRepository;
        this.siteRepository = siteRepository;
        this.overtimeRepository = overtimeRepository;
        this.activeWorkerCache = activeWorkerCache;
    }

    @Override
    @Transactional
    public AttendanceLogDto clockIn(ClockInRequest request) {
        log.info("Clock-in requested for worker ID: {} at site ID: {}", request.getWorkerId(), request.getSiteId());

        Worker worker = workerRepository.findById(request.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found with ID: " + request.getWorkerId()));

        if (!worker.isActive()) {
            throw new BadRequestException("Cannot clock in inactive worker.");
        }

        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with ID: " + request.getSiteId()));

        if (!site.isActive()) {
            throw new BadRequestException("Cannot clock in at inactive site.");
        }

        // Check if already clocked in
        Optional<AttendanceLog> activeLog = attendanceRepository.findByWorkerIdAndClockOutIsNull(worker.getId());
        if (activeLog.isPresent()) {
            throw new ConflictException("DUPLICATE_CLOCK_IN",
                    String.format("Worker is already clocked in at Site: %s", activeLog.get().getSite().getSiteName()));
        }

        LocalDateTime now = LocalDateTime.now();

        AttendanceLog attendanceLog = AttendanceLog.builder()
                .worker(worker)
                .site(site)
                .clockIn(now)
                .flagged(false)
                .build();

        attendanceLog = attendanceRepository.save(attendanceLog);

        // Redis cache active worker
        ActiveWorkerEntry cacheEntry = ActiveWorkerEntry.builder()
                .workerId(worker.getId())
                .workerName(worker.getName())
                .phone(worker.getPhone())
                .designation(worker.getDesignation().name())
                .siteId(site.getId())
                .siteName(site.getSiteName())
                .clockInTime(now.toString())
                .build();

        activeWorkerCache.add(cacheEntry);

        return mapToDto(attendanceLog);
    }

    @Override
    @Transactional
    public AttendanceLogDto clockOut(ClockOutRequest request) {
        log.info("Clock-out requested for worker ID: {}", request.getWorkerId());

        Worker worker = workerRepository.findById(request.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found with ID: " + request.getWorkerId()));

        AttendanceLog activeLog = attendanceRepository.findByWorkerIdAndClockOutIsNull(worker.getId())
                .orElseThrow(() -> new ConflictException("NOT_CLOCKED_IN", "Worker is not currently clocked in."));

        LocalDateTime clockOutTime = LocalDateTime.now();
        activeLog.setClockOut(clockOutTime);

        // Calculate hours
        long durationSeconds = Duration.between(activeLog.getClockIn(), clockOutTime).getSeconds();
        BigDecimal totalHours = BigDecimal.valueOf(durationSeconds)
                .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
        activeLog.setTotalHours(totalHours);

        // Shift duration flag (> 16 hours)
        if (totalHours.compareTo(BigDecimal.valueOf(16)) > 0) {
            activeLog.setFlagged(true);
            log.warn("Worker {} shift exceeded 16 hours ({}) and has been flagged.", worker.getId(), totalHours);
        }

        // Calculate Overtime
        BigDecimal standardShiftHours = BigDecimal.valueOf(8);
        BigDecimal overtimeHours = BigDecimal.ZERO;

        if (totalHours.compareTo(standardShiftHours) > 0) {
            overtimeHours = totalHours.subtract(standardShiftHours);
        }

        activeLog.setOvertimeHours(BigDecimal.ZERO);

        if (overtimeHours.compareTo(BigDecimal.ZERO) > 0) {
            // Apply monthly overtime cap of 60 hours
            String monthString = clockOutTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            BigDecimal currentMonthOvertime = overtimeRepository.sumOvertimeHoursByWorkerAndMonth(worker.getId(), monthString);
            BigDecimal remainingHours = BigDecimal.valueOf(60).subtract(currentMonthOvertime);

            if (remainingHours.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Worker {} has reached monthly 60-hour overtime cap. 0 overtime hours applied to this log.", worker.getId());
                overtimeHours = BigDecimal.ZERO;
            } else if (overtimeHours.compareTo(remainingHours) > 0) {
                log.warn("Worker {} overtime capped from {} to {} remaining hours.", worker.getId(), overtimeHours, remainingHours);
                overtimeHours = remainingHours;
            }

            if (overtimeHours.compareTo(BigDecimal.ZERO) > 0) {
                activeLog.setOvertimeHours(overtimeHours);

                // Overtime Rate and Amount Calculations
                // Standard Hourly Rate = daily wage / 8
                BigDecimal hourlyRate = worker.getDailyWageRate().divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP);
                BigDecimal overtimeAmount = BigDecimal.ZERO;
                BigDecimal firstLimit = BigDecimal.valueOf(2);

                if (overtimeHours.compareTo(firstLimit) <= 0) {
                    // 1.5x for first 2 hours
                    BigDecimal rate = hourlyRate.multiply(BigDecimal.valueOf(1.5));
                    overtimeAmount = overtimeHours.multiply(rate);
                } else {
                    // 1.5x for first 2 hours + 2.0x for subsequent hours
                    BigDecimal rate1 = hourlyRate.multiply(BigDecimal.valueOf(1.5));
                    BigDecimal rate2 = hourlyRate.multiply(BigDecimal.valueOf(2.0));
                    BigDecimal amountForFirst2 = firstLimit.multiply(rate1);
                    BigDecimal amountForRest = overtimeHours.subtract(firstLimit).multiply(rate2);
                    overtimeAmount = amountForFirst2.add(amountForRest);
                }

                // Create OvertimeEntry
                OvertimeEntry overtimeEntry = OvertimeEntry.builder()
                        .worker(worker)
                        .attendance(activeLog)
                        .date(clockOutTime.toLocalDate())
                        .overtimeHours(overtimeHours)
                        .overtimeRateApplied(hourlyRate) // Storing the base hourly rate applied
                        .amount(overtimeAmount.setScale(2, RoundingMode.HALF_UP))
                        .settlementStatus(SettlementStatus.PENDING)
                        .monthString(monthString)
                        .build();

                overtimeRepository.save(overtimeEntry);
                log.info("Recorded OvertimeEntry of {} hours, amount: ₹{} for Worker ID: {}", overtimeHours, overtimeAmount, worker.getId());
            }
        }

        activeLog = attendanceRepository.save(activeLog);

        // Evict from active cache
        activeWorkerCache.remove(worker.getId());

        return mapToDto(activeLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceLogDto> getActiveWorkers() {
        log.info("Fetching active workers...");
        
        // Attempt to load from Redis
        Optional<List<ActiveWorkerEntry>> cachedList = activeWorkerCache.getAll();
        if (cachedList.isPresent()) {
            log.info("Serving active workers from Redis cache. Count: {}", cachedList.get().size());
            return cachedList.get().stream()
                    .map(this::mapCachedToDto)
                    .collect(Collectors.toList());
        }

        // Redis is down, fall back to Database
        log.warn("Redis is unavailable. Falling back to database to load active workers.");
        List<AttendanceLog> activeLogs = attendanceRepository.findAllActiveLogs();
        return activeLogs.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AttendanceLogDto> getAttendanceLogs(Long workerId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        log.info("Fetching paginated attendance logs - Worker ID: {}, From: {}, To: {}, Page: {}, Size: {}", 
                workerId, from, to, pageable.getPageNumber(), pageable.getPageSize());

        Page<AttendanceLog> page = attendanceRepository.findByFilters(workerId, from, to, pageable);
        
        Page<AttendanceLogDto> dtoPage = page.map(this::mapToDto);
        return PageResponse.fromPage(dtoPage);
    }

    private AttendanceLogDto mapToDto(AttendanceLog log) {
        return AttendanceLogDto.builder()
                .id(log.getId())
                .workerId(log.getWorker().getId())
                .workerName(log.getWorker().getName())
                .workerPhone(log.getWorker().getPhone())
                .workerDesignation(log.getWorker().getDesignation().name())
                .siteId(log.getSite().getId())
                .siteName(log.getSite().getSiteName())
                .siteLocation(log.getSite().getLocation())
                .clockIn(log.getClockIn())
                .clockOut(log.getClockOut())
                .totalHours(log.getTotalHours())
                .overtimeHours(log.getOvertimeHours())
                .flagged(log.isFlagged())
                .build();
    }

    private AttendanceLogDto mapCachedToDto(ActiveWorkerEntry cached) {
        return AttendanceLogDto.builder()
                .workerId(cached.getWorkerId())
                .workerName(cached.getWorkerName())
                .workerPhone(cached.getPhone())
                .workerDesignation(cached.getDesignation())
                .siteId(cached.getSiteId())
                .siteName(cached.getSiteName())
                .clockIn(LocalDateTime.parse(cached.getClockInTime()))
                .build();
    }
}
