package com.hrm.workforce.controller;

import com.hrm.workforce.dto.ApiResponse;
import com.hrm.workforce.dto.WorkerDto;
import com.hrm.workforce.service.WorkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workers")
@Tag(name = "Worker Management", description = "Endpoints for managing worker profiles (Admin/HR access)")
public class WorkerController {

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @PostMapping
    @Operation(summary = "Create a new worker profile")
    public ResponseEntity<ApiResponse<WorkerDto>> createWorker(@Valid @RequestBody WorkerDto dto) {
        WorkerDto created = workerService.createWorker(dto);
        return new ResponseEntity<>(ApiResponse.success("Worker created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing worker profile")
    public ResponseEntity<ApiResponse<WorkerDto>> updateWorker(@PathVariable Long id, @Valid @RequestBody WorkerDto dto) {
        WorkerDto updated = workerService.updateWorker(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Worker updated successfully", updated));
    }

    @GetMapping
    @Operation(summary = "Fetch all worker profiles")
    public ResponseEntity<ApiResponse<List<WorkerDto>>> getAllWorkers() {
        List<WorkerDto> workers = workerService.getAllWorkers();
        return ResponseEntity.ok(ApiResponse.success("Workers fetched successfully", workers));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch worker profile by ID")
    public ResponseEntity<ApiResponse<WorkerDto>> getWorkerById(@PathVariable Long id) {
        WorkerDto worker = workerService.getWorkerById(id);
        return ResponseEntity.ok(ApiResponse.success("Worker fetched successfully", worker));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate/Soft-delete a worker profile")
    public ResponseEntity<ApiResponse<String>> deleteWorker(@PathVariable Long id) {
        workerService.deleteWorker(id);
        return ResponseEntity.ok(ApiResponse.success("Worker deactivated successfully"));
    }
}
