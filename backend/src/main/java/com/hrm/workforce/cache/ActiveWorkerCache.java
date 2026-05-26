package com.hrm.workforce.cache;

import java.util.List;
import java.util.Optional;

public interface ActiveWorkerCache {
    void add(ActiveWorkerEntry entry);
    void remove(Long workerId);
    Optional<List<ActiveWorkerEntry>> getAll();
    void evictWorker(Long workerId);
    boolean isAvailable();
}
