package com.hrm.workforce.service.impl;

import com.hrm.workforce.cache.ActiveWorkerCache;
import com.hrm.workforce.dto.WorkerDto;
import com.hrm.workforce.entity.Worker;
import com.hrm.workforce.exception.BadRequestException;
import com.hrm.workforce.exception.ConflictException;
import com.hrm.workforce.exception.ResourceNotFoundException;
import com.hrm.workforce.repository.WorkerRepository;
import com.hrm.workforce.service.WorkerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkerServiceImpl implements WorkerService {

    private final WorkerRepository workerRepository;
    private final ActiveWorkerCache activeWorkerCache;

    public WorkerServiceImpl(WorkerRepository workerRepository, ActiveWorkerCache activeWorkerCache) {
        this.workerRepository = workerRepository;
        this.activeWorkerCache = activeWorkerCache;
    }

    @Override
    @Transactional
    public WorkerDto createWorker(WorkerDto dto) {
        if (workerRepository.existsByPhone(dto.getPhone())) {
            throw new ConflictException("PHONE_ALREADY_EXISTS", "Worker with phone " + dto.getPhone() + " already exists.");
        }

        Worker worker = Worker.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .designation(dto.getDesignation())
                .dailyWageRate(dto.getDailyWageRate())
                .active(dto.isActive())
                .build();

        worker = workerRepository.save(worker);
        return mapToDto(worker);
    }

    @Override
    @Transactional
    public WorkerDto updateWorker(Long id, WorkerDto dto) {
        Worker worker = workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found with ID: " + id));

        if (workerRepository.existsByPhoneAndIdNot(dto.getPhone(), id)) {
            throw new ConflictException("PHONE_ALREADY_EXISTS", "Another worker with phone " + dto.getPhone() + " already exists.");
        }

        boolean changed = !worker.getName().equals(dto.getName())
                || !worker.getPhone().equals(dto.getPhone())
                || worker.getDesignation() != dto.getDesignation()
                || !worker.getDailyWageRate().equals(dto.getDailyWageRate())
                || worker.isActive() != dto.isActive();

        worker.setName(dto.getName());
        worker.setPhone(dto.getPhone());
        worker.setDesignation(dto.getDesignation());
        worker.setDailyWageRate(dto.getDailyWageRate());
        worker.setActive(dto.isActive());

        worker = workerRepository.save(worker);

        // If something changed, invalidate the cache entry to prevent stale read of profile data
        if (changed) {
            activeWorkerCache.evictWorker(id);
        }

        return mapToDto(worker);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkerDto> getAllWorkers() {
        return workerRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WorkerDto getWorkerById(Long id) {
        Worker worker = workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found with ID: " + id));
        return mapToDto(worker);
    }

    @Override
    @Transactional
    public void deleteWorker(Long id) {
        Worker worker = workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found with ID: " + id));
        worker.setActive(false);
        workerRepository.save(worker);
        activeWorkerCache.evictWorker(id);
    }

    private WorkerDto mapToDto(Worker worker) {
        return WorkerDto.builder()
                .id(worker.getId())
                .name(worker.getName())
                .phone(worker.getPhone())
                .designation(worker.getDesignation())
                .dailyWageRate(worker.getDailyWageRate())
                .active(worker.isActive())
                .build();
    }
}
