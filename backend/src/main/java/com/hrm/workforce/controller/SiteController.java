package com.hrm.workforce.controller;

import com.hrm.workforce.dto.ApiResponse;
import com.hrm.workforce.dto.SiteDto;
import com.hrm.workforce.service.SiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
@Tag(name = "Site Management", description = "Endpoints for managing site directories (Admin/HR access)")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @PostMapping
    @Operation(summary = "Create a new construction site")
    public ResponseEntity<ApiResponse<SiteDto>> createSite(@Valid @RequestBody SiteDto dto) {
        SiteDto created = siteService.createSite(dto);
        return new ResponseEntity<>(ApiResponse.success("Site created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update construction site details")
    public ResponseEntity<ApiResponse<SiteDto>> updateSite(@PathVariable Long id, @Valid @RequestBody SiteDto dto) {
        SiteDto updated = siteService.updateSite(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Site updated successfully", updated));
    }

    @GetMapping
    @Operation(summary = "Fetch all construction sites")
    public ResponseEntity<ApiResponse<List<SiteDto>>> getAllSites() {
        List<SiteDto> sites = siteService.getAllSites();
        return ResponseEntity.ok(ApiResponse.success("Sites fetched successfully", sites));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch site by ID")
    public ResponseEntity<ApiResponse<SiteDto>> getSiteById(@PathVariable Long id) {
        SiteDto site = siteService.getSiteById(id);
        return ResponseEntity.ok(ApiResponse.success("Site fetched successfully", site));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate/Soft-delete a construction site")
    public ResponseEntity<ApiResponse<String>> deleteSite(@PathVariable Long id) {
        siteService.deleteSite(id);
        return ResponseEntity.ok(ApiResponse.success("Site deactivated successfully"));
    }
}
