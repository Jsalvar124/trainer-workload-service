package com.jsalva.trainerworkload.repository;

import com.jsalva.trainerworkload.entity.TrainerSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerSummaryRepository extends JpaRepository<TrainerSummary, Long> {
}
