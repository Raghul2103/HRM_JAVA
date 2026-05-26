package com.hrm.workforce.service;

import com.hrm.workforce.dto.AttendanceLogDto;
import com.hrm.workforce.dto.ClockInRequest;
import com.hrm.workforce.dto.ClockOutRequest;
import com.hrm.workforce.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceService {
    AttendanceLogDto clockIn(ClockInRequest request);
    AttendanceLogDto clockOut(ClockOutRequest request);
    List<AttendanceLogDto> getActiveWorkers();
    PageResponse<AttendanceLogDto> getAttendanceLogs(Long workerId, LocalDateTime from, LocalDateTime to, Pageable pageable);
}
