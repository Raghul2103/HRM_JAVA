package com.hrm.workforce.controller;

import com.hrm.workforce.dto.*;
import com.hrm.workforce.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@Tag(name = "Attendance Tracking", description = "Endpoints for clock-in/out and active logs")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/clock-in")
    @Operation(summary = "Log worker arrival at a site")
    public ResponseEntity<ApiResponse<AttendanceLogDto>> clockIn(@Valid @RequestBody ClockInRequest request) {
        AttendanceLogDto dto = attendanceService.clockIn(request);
        return ResponseEntity.ok(ApiResponse.success("Worker clocked in successfully", dto));
    }

    @PostMapping("/clock-out")
    @Operation(summary = "Log worker departure and calculate overtime")
    public ResponseEntity<ApiResponse<AttendanceLogDto>> clockOut(@Valid @RequestBody ClockOutRequest request) {
        AttendanceLogDto dto = attendanceService.clockOut(request);
        return ResponseEntity.ok(ApiResponse.success("Worker clocked out successfully", dto));
    }

    @GetMapping("/active")
    @Operation(summary = "List all active clocked-in workers (cached in Redis)")
    public ResponseEntity<ApiResponse<List<AttendanceLogDto>>> getActiveWorkers() {
        List<AttendanceLogDto> activeWorkers = attendanceService.getActiveWorkers();
        return ResponseEntity.ok(ApiResponse.success("Active workers fetched successfully", activeWorkers));
    }

    @GetMapping("/log")
    @Operation(summary = "Search history of attendance logs with pagination (N+1 query optimized)")
    public ResponseEntity<ApiResponse<PageResponse<AttendanceLogDto>>> getAttendanceLogs(
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // Enforce safety limits
        if (size > 100) {
            size = 100;
        }
        if (size <= 0) {
            size = 20;
        }
        if (page < 0) {
            page = 0;
        }

        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.atTime(LocalTime.MAX) : null;

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<AttendanceLogDto> logs = attendanceService.getAttendanceLogs(workerId, fromDateTime, toDateTime, pageable);

        return ResponseEntity.ok(ApiResponse.success("Attendance logs fetched successfully", logs));
    }
}
