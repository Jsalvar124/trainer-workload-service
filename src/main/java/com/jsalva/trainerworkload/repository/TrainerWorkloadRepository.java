package com.jsalva.trainerworkload.repository;

import com.jsalva.trainerworkload.domain.TrainerMonthlyWorkload;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerWorkloadRepository extends MongoRepository<TrainerMonthlyWorkload, String> {

    Optional<TrainerMonthlyWorkload> findByUsername(String username);
    // Check if exists

    boolean existsByUsername(String username);

}
