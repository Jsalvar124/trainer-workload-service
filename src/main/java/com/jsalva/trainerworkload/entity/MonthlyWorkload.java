package com.jsalva.trainerworkload.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "monthly_workload",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_trainer_year_month",
                columnNames = {"trainer_summary_id", "workload_year", "workload_month"}
        ))
public class MonthlyWorkload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_summary_id", nullable = false)
    private TrainerSummary trainerSummary;
    @Column(name = "workload_year", nullable = false)
    @Positive(message = "Year must be positive")
    private Integer year;
    @Column(name = "workload_month", nullable = false)
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;
    @Column(name = "workload_minutes", nullable = false)
    @Positive(message = "Workload minutes must be positive")
    private Integer totalWorkload;

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
        return totalWorkload;
    }

    public void setWorkloadHours(Integer workloadHours) {
        this.totalWorkload = workloadHours;
    }

    @Override
    public String toString() {
        return "MonthlyWorkload{" +
                "id=" + id +
                ", trainerSummary=" + trainerSummary +
                ", year=" + year +
                ", month=" + month +
                ", workloadHours=" + totalWorkload +
                '}';
    }
}
