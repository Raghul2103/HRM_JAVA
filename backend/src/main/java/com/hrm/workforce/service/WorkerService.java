package com.hrm.workforce.service;

import com.hrm.workforce.dto.WorkerDto;
import java.util.List;

public interface WorkerService {
    WorkerDto createWorker(WorkerDto dto);
    WorkerDto updateWorker(Long id, WorkerDto dto);
    List<WorkerDto> getAllWorkers();
    WorkerDto getWorkerById(Long id);
    void deleteWorker(Long id);
}
