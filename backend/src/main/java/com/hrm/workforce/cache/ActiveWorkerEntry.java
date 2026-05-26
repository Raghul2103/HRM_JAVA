package com.hrm.workforce.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveWorkerEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long workerId;
    private String workerName;
    private String phone;
    private String designation;
    private Long siteId;
    private String siteName;
    private String clockInTime;
}
