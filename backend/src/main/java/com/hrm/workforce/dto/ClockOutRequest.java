package com.hrm.workforce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClockOutRequest {
    @NotNull(message = "Worker ID is required")
    private Long workerId;
}
