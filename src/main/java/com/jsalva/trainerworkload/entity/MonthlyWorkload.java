package com.jsalva.trainerworkload.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "monthly_workload",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_trainer_year_month",
                columnNames = {"trainer_summary_id", "year", "month"}
        ))
public class MonthlyWorkload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_summary_id", nullable = false)
    private TrainerSummary trainerSummary;
    @Column(name = "year", nullable = false)
    private Integer year;
    @Column(name = "month", nullable = false)
    private Integer month;
    @Column(name = "workload_hours", nullable = false)
    @Positive(message = "Workload hours must be positive")
    private Integer workloadHours;

    public MonthlyWorkload() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TrainerSummary getTrainerSummary() {
        return trainerSummary;
    }

    public void setTrainerSummary(TrainerSummary trainerSummary) {
        this.trainerSummary = trainerSummary;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getWorkloadHours() {
        return workloadHours;
    }

    public void setWorkloadHours(Integer workloadHours) {
        this.workloadHours = workloadHours;
    }

    @Override
    public String toString() {
        return "MonthlyWorkload{" +
                "id=" + id +
                ", trainerSummary=" + trainerSummary +
                ", year=" + year +
                ", month=" + month +
                ", workloadHours=" + workloadHours +
                '}';
    }
}
