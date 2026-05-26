package com.hrm.workforce.controller;

import com.hrm.workforce.dto.ApiResponse;
import com.hrm.workforce.dto.OvertimeSummaryDto;
import com.hrm.workforce.dto.WorkerDto;
import com.hrm.workforce.entity.Designation;
import com.hrm.workforce.exception.BadRequestException;
import com.hrm.workforce.service.GovernmentWageService;
import com.hrm.workforce.service.OvertimeService;
import com.hrm.workforce.service.WorkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/overtime")
@Tag(name = "Overtime Management", description = "Endpoints for monthly overtime summaries and settlements")
@Slf4j
public class OvertimeController {

    private final OvertimeService overtimeService;
    private final WorkerService workerService;
    private final GovernmentWageService governmentWageService;

    public OvertimeController(OvertimeService overtimeService,
                              WorkerService workerService,
                              GovernmentWageService governmentWageService) {
        this.overtimeService = overtimeService;
        this.workerService = workerService;
        this.governmentWageService = governmentWageService;
    }

    @GetMapping("/summary/{workerId}")
    @Operation(summary = "Get monthly overtime summary and breakdown for a worker")
    public ResponseEntity<ApiResponse<OvertimeSummaryDto>> getOvertimeSummary(
            @PathVariable Long workerId,
            @RequestParam String month
    ) {
        if (month == null || !month.matches("^\\d{4}-\\d{2}$")) {
            throw new BadRequestException("Month must be in YYYY-MM format");
        }
        OvertimeSummaryDto summary = overtimeService.getOvertimeSummary(workerId, month);
        return ResponseEntity.ok(ApiResponse.success("Overtime summary fetched successfully", summary));
    }

    @PostMapping("/settle/{workerId}")
    @Operation(summary = "Settle monthly overtime for a worker (Validates against Gov standard and triggers SMS)")
    public ResponseEntity<ApiResponse<BigDecimal>> settleOvertime(
            @PathVariable Long workerId,
            @RequestParam String month
    ) {
        if (month == null || !month.matches("^\\d{4}-\\d{2}$")) {
            throw new BadRequestException("Month must be in YYYY-MM format");
        }

        // 1. Fetch Worker Details first to get Designation
        // This is non-transactional, no DB connection is checked out for the slow external call!
        WorkerDto workerDto = workerService.getWorkerById(workerId);
        Designation designation = workerDto.getDesignation();

        // 2. Fetch government wage standard rate from slow external API
        // This is a slow external call. Doing it here prevents connection pool starvation
        // as no DB transaction has started yet! (fixes LF-205)
        log.info("Controller calling external GovernmentWageService outside transactional boundary...");
        BigDecimal govWageStandard = governmentWageService.getMinimumWageRate(designation);

        // 3. Perform the actual database transactional settlement
        log.info("Controller calling transactional OvertimeService to settle...");
        BigDecimal totalSettledAmount = overtimeService.settleOvertime(workerId, month, govWageStandard);

        return ResponseEntity.ok(ApiResponse.success("Overtime settled successfully", totalSettledAmount));
    }
}
