package com.jsalva.trainerworkload.repository;

import com.jsalva.trainerworkload.domain.MonthlyWorkload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyWorkloadRepository extends JpaRepository<MonthlyWorkload, Long> {

    // Query by username, _acts as ., TrainerSummary.username
    List<MonthlyWorkload> findByTrainerSummary_Username(
            String username
    );

    // Query by username, _acts as ., TrainerSummary.username
    List<MonthlyWorkload> findByTrainerSummary_UsernameAndYear(
            String username,
            Integer year
    );

    // Query by username, _acts as ., TrainerSummary.username
    Optional<MonthlyWorkload> findByTrainerSummary_UsernameAndYearAndMonth(
            String username,
            Integer year,
            Integer month

    );


}
