package com.hrm.workforce.dto;

import com.hrm.workforce.entity.Designation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerDto {
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotNull(message = "Designation is required")
    private Designation designation;

    @NotNull(message = "Daily wage rate is required")
    @Positive(message = "Daily wage rate must be positive")
    private BigDecimal dailyWageRate;

    private boolean active;
}
