package com.hrm.workforce.repository;

import com.hrm.workforce.entity.AttendanceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceLog, Long> {

    Optional<AttendanceLog> findByWorkerIdAndClockOutIsNull(Long workerId);

    @Query("SELECT a FROM AttendanceLog a JOIN FETCH a.worker JOIN FETCH a.site WHERE a.clockOut IS NULL")
    List<AttendanceLog> findAllActiveLogs();

    @Query(value = "SELECT a FROM AttendanceLog a JOIN FETCH a.worker JOIN FETCH a.site " +
                   "WHERE (:workerId IS NULL OR a.worker.id = :workerId) " +
                   "AND (cast(:from as timestamp) IS NULL OR a.clockIn >= :from) " +
                   "AND (cast(:to as timestamp) IS NULL OR a.clockIn <= :to)",
           countQuery = "SELECT COUNT(a) FROM AttendanceLog a " +
                        "WHERE (:workerId IS NULL OR a.worker.id = :workerId) " +
                        "AND (cast(:from as timestamp) IS NULL OR a.clockIn >= :from) " +
                        "AND (cast(:to as timestamp) IS NULL OR a.clockIn <= :to)")
    Page<AttendanceLog> findByFilters(@Param("workerId") Long workerId,
                                      @Param("from") LocalDateTime from,
                                      @Param("to") LocalDateTime to,
                                      Pageable pageable);
}
