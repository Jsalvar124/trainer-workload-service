package com.jsalva.trainerworkload.cucumber.integration;

import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("test")
public class CucumberIntegrationConfig {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Before
    public void cleanDatabase() {
        // Clean MongoDB before each scenario
        mongoTemplate.getDb().drop();
    }
}
