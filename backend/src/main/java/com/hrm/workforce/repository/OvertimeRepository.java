package com.hrm.workforce.repository;

import com.hrm.workforce.entity.OvertimeEntry;
import com.hrm.workforce.entity.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OvertimeRepository extends JpaRepository<OvertimeEntry, Long> {

    @Query("SELECT o FROM OvertimeEntry o JOIN FETCH o.worker JOIN FETCH o.attendance WHERE o.worker.id = :workerId AND o.monthString = :monthString")
    List<OvertimeEntry> findByWorkerIdAndMonthString(@Param("workerId") Long workerId, @Param("monthString") String monthString);

    @Query("SELECT o FROM OvertimeEntry o WHERE o.worker.id = :workerId AND o.monthString = :monthString AND o.settlementStatus = :status")
    List<OvertimeEntry> findByWorkerIdAndMonthStringAndSettlementStatus(@Param("workerId") Long workerId, @Param("monthString") String monthString, @Param("status") SettlementStatus status);

    @Query("SELECT COALESCE(SUM(o.overtimeHours), 0) FROM OvertimeEntry o WHERE o.worker.id = :workerId AND o.monthString = :monthString")
    BigDecimal sumOvertimeHoursByWorkerAndMonth(@Param("workerId") Long workerId, @Param("monthString") String monthString);
}
