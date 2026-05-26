package com.hrm.workforce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClockInRequest {
    @NotNull(message = "Worker ID is required")
    private Long workerId;

    @NotNull(message = "Site ID is required")
    private Long siteId;
}
