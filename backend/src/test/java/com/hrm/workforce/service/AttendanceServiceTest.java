package com.hrm.workforce.service;

import com.hrm.workforce.cache.ActiveWorkerCache;
import com.hrm.workforce.cache.ActiveWorkerEntry;
import com.hrm.workforce.dto.AttendanceLogDto;
import com.hrm.workforce.dto.ClockInRequest;
import com.hrm.workforce.dto.ClockOutRequest;
import com.hrm.workforce.entity.*;
import com.hrm.workforce.exception.ConflictException;
import com.hrm.workforce.repository.AttendanceRepository;
import com.hrm.workforce.repository.OvertimeRepository;
import com.hrm.workforce.repository.SiteRepository;
import com.hrm.workforce.repository.WorkerRepository;
import com.hrm.workforce.service.impl.AttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private WorkerRepository workerRepository;
    @Mock
    private SiteRepository siteRepository;
    @Mock
    private OvertimeRepository overtimeRepository;
    @Mock
    private ActiveWorkerCache activeWorkerCache;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Worker worker;
    private Site site;
    private AttendanceLog activeLog;

    @BeforeEach
    void setUp() {
        worker = Worker.builder()
                .id(1L)
                .name("Rajesh Kumar")
                .phone("9876543210")
                .designation(Designation.MASON)
                .dailyWageRate(new BigDecimal("500.00"))
                .active(true)
                .build();

        site = Site.builder()
                .id(1L)
                .siteName("Greenfield Phase 2")
                .location("Sector 62, Noida")
                .active(true)
                .build();

        activeLog = AttendanceLog.builder()
                .id(10L)
                .worker(worker)
                .site(site)
                .clockIn(LocalDateTime.now().minusHours(9)) // Standard shift + 1h overtime
                .flagged(false)
                .build();
    }

    @Test
    void testClockInSuccess() {
        ClockInRequest request = new ClockInRequest();
        request.setWorkerId(1L);
        request.setSiteId(1L);

        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(siteRepository.findById(1L)).thenReturn(Optional.of(site));
        when(attendanceRepository.findByWorkerIdAndClockOutIsNull(1L)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceLogDto dto = attendanceService.clockIn(request);

        assertNotNull(dto);
        assertEquals(1L, dto.getWorkerId());
        assertEquals(1L, dto.getSiteId());
        verify(activeWorkerCache, times(1)).add(any(ActiveWorkerEntry.class));
    }

    @Test
    void testClockInAlreadyClockedInThrowsConflict() {
        ClockInRequest request = new ClockInRequest();
        request.setWorkerId(1L);
        request.setSiteId(1L);

        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(siteRepository.findById(1L)).thenReturn(Optional.of(site));
        when(attendanceRepository.findByWorkerIdAndClockOutIsNull(1L)).thenReturn(Optional.of(activeLog));

        assertThrows(ConflictException.class, () -> attendanceService.clockIn(request));
    }

    @Test
    void testClockOutAndOvertimeMath() {
        ClockOutRequest request = new ClockOutRequest();
        request.setWorkerId(1L);

        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(attendanceRepository.findByWorkerIdAndClockOutIsNull(1L)).thenReturn(Optional.of(activeLog));
        when(overtimeRepository.sumOvertimeHoursByWorkerAndMonth(eq(1L), anyString())).thenReturn(BigDecimal.ZERO);
        when(attendanceRepository.save(any(AttendanceLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceLogDto dto = attendanceService.clockOut(request);

        assertNotNull(dto);
        assertNotNull(dto.getClockOut());
        assertTrue(dto.getTotalHours().compareTo(BigDecimal.valueOf(9)) >= 0);
        // Standard shift is 8h, so overtime should be calculated
        verify(overtimeRepository, times(1)).save(any(OvertimeEntry.class));
        verify(activeWorkerCache, times(1)).remove(1L);
    }

    @Test
    void testGetActiveWorkersRedisFallbackToDB() {
        // When Redis is down, activeWorkerCache.getAll() returns Optional.empty()
        when(activeWorkerCache.getAll()).thenReturn(Optional.empty());
        when(attendanceRepository.findAllActiveLogs()).thenReturn(Collections.singletonList(activeLog));

        List<AttendanceLogDto> list = attendanceService.getActiveWorkers();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Rajesh Kumar", list.get(0).getWorkerName());
        verify(attendanceRepository, times(1)).findAllActiveLogs();
    }
}
