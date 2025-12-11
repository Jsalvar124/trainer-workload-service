package com.jsalva.trainerworkload.repository;

import com.jsalva.trainerworkload.entity.MonthlyWorkload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyWorkloadRepository extends JpaRepository<MonthlyWorkload, Long> {
}
