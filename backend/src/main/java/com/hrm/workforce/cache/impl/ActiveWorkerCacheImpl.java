package com.hrm.workforce.cache.impl;

import com.hrm.workforce.cache.ActiveWorkerCache;
import com.hrm.workforce.cache.ActiveWorkerEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ActiveWorkerCacheImpl implements ActiveWorkerCache {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "active_worker:";
    private static final long TTL_HOURS = 16;

    public ActiveWorkerCacheImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void add(ActiveWorkerEntry entry) {
        try {
            String key = KEY_PREFIX + entry.getWorkerId();
            redisTemplate.opsForValue().set(key, entry, TTL_HOURS, TimeUnit.HOURS);
            log.info("Worker {} cached in Redis with 16h TTL", entry.getWorkerId());
        } catch (Exception e) {
            log.error("Redis add failure for worker {}: {}", entry.getWorkerId(), e.getMessage());
        }
    }

    @Override
    public void remove(Long workerId) {
        try {
            String key = KEY_PREFIX + workerId;
            redisTemplate.delete(key);
            log.info("Worker {} removed from Redis cache", workerId);
        } catch (Exception e) {
            log.error("Redis remove failure for worker {}: {}", workerId, e.getMessage());
        }
    }

    @Override
    public Optional<List<ActiveWorkerEntry>> getAll() {
        try {
            Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return Optional.of(Collections.emptyList());
            }
            List<ActiveWorkerEntry> entries = new ArrayList<>();
            for (String key : keys) {
                ActiveWorkerEntry entry = (ActiveWorkerEntry) redisTemplate.opsForValue().get(key);
                if (entry != null) {
                    entries.add(entry);
                }
            }
            return Optional.of(entries);
        } catch (Exception e) {
            log.error("Redis getAll failure: {}", e.getMessage());
            return Optional.empty(); // fallback trigger
        }
    }

    @Override
    public void evictWorker(Long workerId) {
        try {
            String key = KEY_PREFIX + workerId;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                redisTemplate.delete(key);
                log.info("Evicted worker {} from active cache due to profile changes", workerId);
            }
        } catch (Exception e) {
            log.error("Redis evict failure for worker {}: {}", workerId, e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
