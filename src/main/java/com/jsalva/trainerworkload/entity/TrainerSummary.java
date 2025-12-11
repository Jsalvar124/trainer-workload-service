package com.jsalva.trainerworkload.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

//1. Trainer Username
//2. Trainer First Name
//3. Trainer Last Name
//4. IsActive
//5. Training date
//6. Training duration
//7. Action Type (ADD/DELETE)

@Entity
@Table(name = "trainer_workloads")
public class TrainerSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "username", nullable = false)
    private String username;
    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "last_name", nullable = false)
    private String lastName;
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    @Column(name = "monthly_workload")
    @OneToMany(mappedBy = "trainerSummary")
    private List<MonthlyWorkload> monthlyWorkloads;

    public TrainerSummary() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }


}
