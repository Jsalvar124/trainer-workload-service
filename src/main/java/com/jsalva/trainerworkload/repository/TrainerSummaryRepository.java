package com.jsalva.trainerworkload.repository;

import com.jsalva.trainerworkload.entity.TrainerSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerSummaryRepository extends JpaRepository<TrainerSummary, Long> {

    Optional<TrainerSummary> findByUsername(String username);
    // Check if exists
    boolean existsByUsername(String username);

}
