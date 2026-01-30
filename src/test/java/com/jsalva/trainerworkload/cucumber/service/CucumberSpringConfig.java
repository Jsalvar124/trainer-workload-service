package com.jsalva.trainerworkload.cucumber.service;

import com.jsalva.trainerworkload.repository.TrainerWorkloadRepository;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@CucumberContextConfiguration
@SpringBootTest
public class CucumberSpringConfig {
    @MockitoBean
    private TrainerWorkloadRepository trainerWorkloadRepository;

}
