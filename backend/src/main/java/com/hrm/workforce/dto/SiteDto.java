package com.hrm.workforce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteDto {
    private Long id;

    @NotBlank(message = "Site name is required")
    private String siteName;

    @NotBlank(message = "Location is required")
    private String location;

    private boolean active;
}
