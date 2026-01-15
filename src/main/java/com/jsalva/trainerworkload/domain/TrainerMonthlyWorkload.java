package com.jsalva.trainerworkload.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data //Lombok
@Document(collection = "trainer_monthly_workload") //Mongodb Annotation
@CompoundIndexes({
        @CompoundIndex(name = "name_idx", def = "{'firstName': 1, 'lastName': 1}") // both ascending order (1)
})
public class TrainerMonthlyWorkload {
    @Id
    private String id; //Mongo stores id as String

    @Indexed(unique = true)
    private String username;

    // Index is covered by compound index
    private String firstName;
    @Indexed
    private String lastName;
    private Boolean isActive;

    private List<YearSummary> years = new ArrayList<>();;

    // Subdocuments as inner static classes, showing embedding intent.
    @Data
    public static class YearSummary {
        private Integer year;
        private List<MonthSummary> months = new ArrayList<>();;
    }

    @Data
    public static class MonthSummary {
        private Integer month;
        private Integer totalWorkload;
    }
}
