package com.jsalva.trainerworkload.service;

import com.jsalva.trainerworkload.dto.request.TrainerWorkloadCommandMessageDto;
import com.jsalva.trainerworkload.dto.response.MonthSummaryDto;
import com.jsalva.trainerworkload.dto.response.TrainerWorkloadResponseDto;
import com.jsalva.trainerworkload.dto.response.YearSummaryDto;
import com.jsalva.trainerworkload.enums.ActionType;
import com.jsalva.trainerworkload.domain.TrainerMonthlyWorkload;
import com.jsalva.trainerworkload.mapper.TrainerWorkloadMapper;
import com.jsalva.trainerworkload.repository.TrainerWorkloadRepository;
import com.jsalva.trainerworkload.service.impl.TrainerWorkloadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainerWorkloadServiceTest {
    @Mock
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @InjectMocks
    private TrainerWorkloadServiceImpl trainerWorkloadService;

    private TrainerWorkloadMapper trainerWorkloadMapper;  // Real mapper, not mock

    @BeforeEach
    void setUp() {
        trainerWorkloadMapper = new TrainerWorkloadMapper();  // Create real instance
        // Inject it manually into service
        trainerWorkloadService = new TrainerWorkloadServiceImpl(
                trainerWorkloadRepository,
                trainerWorkloadMapper
        );
    }

    @Test
    void addWorkload_existingTrainer_existingMonth() {
        // given
        TrainerMonthlyWorkload trainer = new TrainerMonthlyWorkload();
        trainer.setUsername("Juan.Perez");
        trainer.setFirstName("Juan");
        trainer.setLastName("Perez");
        trainer.setIsActive(true);

        TrainerMonthlyWorkload.YearSummary yearSummary = new TrainerMonthlyWorkload.YearSummary();
        yearSummary.setYear(2026);

        TrainerMonthlyWorkload.MonthSummary monthSummary = new TrainerMonthlyWorkload.MonthSummary();
        monthSummary.setMonth(2);
        monthSummary.setTotalWorkload(100);
        yearSummary.getMonths().add(monthSummary);

        trainer.getYears().add(yearSummary);

        TrainerWorkloadCommandMessageDto dto =
                new TrainerWorkloadCommandMessageDto(
                        "Juan.Perez",
                        "Juan",
                        "Perez",
                        true,
                        LocalDate.of(2026, 2, 15),

                        120
                );

        when(trainerWorkloadRepository.findByUsername("Juan.Perez"))
                .thenReturn(Optional.of(trainer));

        // when
        trainerWorkloadService.updateWorkload(dto, ActionType.ADD);

        // then
        assertEquals(220, monthSummary.getTotalWorkload());  // Same reference
        assertEquals(220, trainer.getYears().get(0).getMonths().get(0).getTotalWorkload());
        verify(trainerWorkloadRepository).save(trainer);
    }

    @Test
    void addWorkload_newTrainer_newMonth() {
        // given
        TrainerWorkloadCommandMessageDto dto = new TrainerWorkloadCommandMessageDto(
                "New.Trainer",
                "New",
                "Trainer",
                true,
                LocalDate.of(2026, 2, 13),
                100
        );

        when(trainerWorkloadRepository.findByUsername("New.Trainer"))
                .thenReturn(Optional.empty());

        when(trainerWorkloadRepository.save(any(TrainerMonthlyWorkload.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        trainerWorkloadService.updateWorkload(dto, ActionType.ADD);

        // then
        ArgumentCaptor<TrainerMonthlyWorkload> trainerCaptor =
                ArgumentCaptor.forClass(TrainerMonthlyWorkload.class);

        verify(trainerWorkloadRepository, times(2)).save(trainerCaptor.capture());
        // times(2): once in findOrCreateTrainerSummary, once in updateWorkload

        TrainerMonthlyWorkload savedTrainer = trainerCaptor.getValue();  // Gets the last saved value

        // Verify trainer fields
        assertEquals("New.Trainer", savedTrainer.getUsername());
        assertEquals("New", savedTrainer.getFirstName());
        assertEquals("Trainer", savedTrainer.getLastName());
        assertTrue(savedTrainer.getIsActive());

        // Verify year was created
        assertEquals(1, savedTrainer.getYears().size());
        TrainerMonthlyWorkload.YearSummary yearSummary = savedTrainer.getYears().get(0);
        assertEquals(2026, yearSummary.getYear());

        // Verify month was created
        assertEquals(1, yearSummary.getMonths().size());
        TrainerMonthlyWorkload.MonthSummary monthSummary = yearSummary.getMonths().get(0);
        assertEquals(2, monthSummary.getMonth());  // February
        assertEquals(100, monthSummary.getTotalWorkload());
    }

    @Test
    void deleteWorkload_existingTrainer_existingMonth() {
        // given
        TrainerMonthlyWorkload trainer = new TrainerMonthlyWorkload();
        trainer.setUsername("Juan.Perez");

        TrainerMonthlyWorkload.YearSummary yearSummary = new TrainerMonthlyWorkload.YearSummary();
        yearSummary.setYear(2026);

        TrainerMonthlyWorkload.MonthSummary monthSummary = new TrainerMonthlyWorkload.MonthSummary();
        monthSummary.setMonth(2);
        monthSummary.setTotalWorkload(220);
        yearSummary.getMonths().add(monthSummary);

        trainer.getYears().add(yearSummary);

        TrainerWorkloadCommandMessageDto dto =
                new TrainerWorkloadCommandMessageDto(
                        "Juan.Perez",
                        "Juan",
                        "Perez",
                        true,
                        LocalDate.of(2026, 2, 10),

                        100
                );

        when(trainerWorkloadRepository.findByUsername("Juan.Perez"))
                .thenReturn(Optional.of(trainer));
        // when
        trainerWorkloadService.updateWorkload(dto, ActionType.DELETE);

        // then
        assertEquals(120, monthSummary.getTotalWorkload());  // 220 - 100
        assertEquals(1, yearSummary.getMonths().size());  // Month still exists
        assertEquals(1, trainer.getYears().size());  // Year still exists
        verify(trainerWorkloadRepository).save(trainer);
    }

    @Test
    void updateWorkload_delete_removesWorkload_whenResultIsZeroOrNegative(){
        // given
        TrainerMonthlyWorkload trainer = new TrainerMonthlyWorkload();
        trainer.setId("asd123");
        trainer.setUsername("Trainer.One");

        TrainerMonthlyWorkload.YearSummary yearSummary = new TrainerMonthlyWorkload.YearSummary();
        yearSummary.setYear(2026);

        TrainerMonthlyWorkload.MonthSummary monthSummary = new TrainerMonthlyWorkload.MonthSummary();
        monthSummary.setMonth(2);
        monthSummary.setTotalWorkload(100);
        yearSummary.getMonths().add(monthSummary);

        trainer.getYears().add(yearSummary);

        TrainerWorkloadCommandMessageDto dto = new TrainerWorkloadCommandMessageDto(
                "Trainer.One",
                "Trainer",
                "One",
                true,
                LocalDate.of(2026, 2, 10),
                100
        );

        when(trainerWorkloadRepository.findByUsername("Trainer.One"))
                .thenReturn(Optional.of(trainer));

        // when
        trainerWorkloadService.updateWorkload(dto, ActionType.DELETE);

        // then - Verify month was removed
        assertTrue(yearSummary.getMonths().isEmpty(), "Month should be removed when workload becomes zero");

        // Verify year was also removed (since no months left)
        assertTrue(trainer.getYears().isEmpty(), "Year should be removed when no months remain");

        // Verify save was called
        verify(trainerWorkloadRepository).save(trainer);
    }

    @Test
    void updateWorkload_nullAction_throwsException() {
        TrainerWorkloadCommandMessageDto request = new TrainerWorkloadCommandMessageDto(
                "Trainer.One",
                "Trainer",
                "One",
                true,
                LocalDate.of(2025, 12, 5),
                60
        );

        TrainerMonthlyWorkload existingTrainer = new TrainerMonthlyWorkload();
        existingTrainer.setUsername("Trainer.One");

        when(trainerWorkloadRepository.findByUsername("Trainer.One"))
                .thenReturn(Optional.of(existingTrainer));  // Return existing, not empty

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainerWorkloadService.updateWorkload(request, null)); // Null action type

        // Verify exception message
        assertTrue(exception.getMessage().contains("Unsupported action"));

        // Verify no save operations occurred
        verify(trainerWorkloadRepository, never()).save(any());
    }

    @Test
    void getWorkload_existingTrainer_returnsAggregatedData() {
        // given
        TrainerMonthlyWorkload trainer = new TrainerMonthlyWorkload();
        trainer.setId("asd123");
        trainer.setUsername("Juan.Perez");
        trainer.setFirstName("Juan");
        trainer.setLastName("Perez");
        trainer.setIsActive(true);

        TrainerMonthlyWorkload.YearSummary yearSummary = new TrainerMonthlyWorkload.YearSummary();
        yearSummary.setYear(2026);

        TrainerMonthlyWorkload.MonthSummary monthSummary = new TrainerMonthlyWorkload.MonthSummary();
        monthSummary.setMonth(2);
        monthSummary.setTotalWorkload(100);
        yearSummary.getMonths().add(monthSummary);

        trainer.getYears().add(yearSummary);

        when(trainerWorkloadRepository.findByUsername("Juan.Perez"))
                .thenReturn(Optional.of(trainer));

        // when
        TrainerWorkloadResponseDto result =
                trainerWorkloadService.getTrainerWorkload("Juan.Perez", null, null);

        // then
        assertEquals("Juan.Perez", result.username());
        assertEquals("Juan", result.firstName());
        assertEquals("Perez", result.lastName());
        assertTrue(result.isActive());

        assertEquals(1, result.yearSummaryDtoList().size());

        YearSummaryDto yearSummaryDto = result.yearSummaryDtoList().getFirst();
        assertEquals(2026, yearSummaryDto.year());

        assertEquals(1, yearSummaryDto.monthSummaryDtoList().size());

        MonthSummaryDto monthSummaryDto = yearSummaryDto.monthSummaryDtoList().getFirst();
        assertEquals(2, monthSummaryDto.month());
        assertEquals(100, monthSummaryDto.totalWorkload());
    }
}
