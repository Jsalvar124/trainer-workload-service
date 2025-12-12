package com.jsalva.trainerworkload.repository;

import com.jsalva.trainerworkload.entity.MonthlyWorkload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonthlyWorkloadRepository extends JpaRepository<MonthlyWorkload, Long> {

    // Query by username, _acts as ., TrainerSummary.username
    Optional<MonthlyWorkload> findByTrainerSummary_UsernameAndYearAndMonth(
            String username,
            Integer year,
            Integer month
    );


}
