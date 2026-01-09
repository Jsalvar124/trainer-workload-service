package com.jsalva.trainerworkload.repository;

import com.jsalva.trainerworkload.domain.TrainerMonthlyWorkload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerSummaryRepository extends JpaRepository<TrainerMonthlyWorkload, Long> {

    Optional<TrainerMonthlyWorkload> findByUsername(String username);
    // Check if exists
    boolean existsByUsername(String username);

}
