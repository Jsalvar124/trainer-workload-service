package com.jsalva.trainerworkload.service;

import com.jsalva.trainerworkload.dto.request.TrainerWorkloadCommandMessageDto;
import com.jsalva.trainerworkload.dto.response.MonthSummaryDto;
import com.jsalva.trainerworkload.dto.response.TrainerWorkloadResponseDto;
import com.jsalva.trainerworkload.dto.response.YearSummaryDto;
import com.jsalva.trainerworkload.enums.ActionType;
import com.jsalva.trainerworkload.domain.TrainerMonthlyWorkload;
import com.jsalva.trainerworkload.repository.TrainerWorkloadRepository;
import com.jsalva.trainerworkload.service.impl.TrainerWorkloadServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainerWorkloadServiceTest {
    @Mock
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @Mock
    private MonthlyWorkloadRepository monthlyWorkloadRepository;

    @InjectMocks
    private TrainerWorkloadServiceImpl trainerWorkloadService;

    @Test
    void addWorkload_existingTrainer_existingMonth() {
        // given
        TrainerMonthlyWorkload trainer = new TrainerMonthlyWorkload();
        trainer.setUsername("Juan.Perez");

        MonthlyWorkload workload = new MonthlyWorkload();
        workload.setTotalWorkload(120);

        TrainerWorkloadCommandMessageDto dto =
                new TrainerWorkloadCommandMessageDto(
                        "Juan.Perez",
                        "Juan",
                        "Perez",
                        true,
                        LocalDate.now(),

                        100
                );

        when(trainerWorkloadRepository.findByUsername("Juan.Perez"))
                .thenReturn(Optional.of(trainer));

        when(monthlyWorkloadRepository
                .findByTrainerSummary_UsernameAndYearAndMonth(trainer.getUsername(), 2025, 12))
                .thenReturn(Optional.of(workload));

        // when
        trainerWorkloadService.updateWorkload(dto, ActionType.ADD);

        // then
        assertEquals(220, workload.getTotalWorkload());
        verify(monthlyWorkloadRepository).save(workload);
    }

    @Test
    void addWorkload_newTrainer_existingMonth() {
        // given
        TrainerWorkloadCommandMessageDto dto =
                new TrainerWorkloadCommandMessageDto(
                        "New.Trainer",
                        "New",
                        "Trainer",
                        true,
                        LocalDate.now(),

                        100
                );

        when(trainerWorkloadRepository.findByUsername("New.Trainer"))
                .thenReturn(Optional.empty());

        when(trainerWorkloadRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(monthlyWorkloadRepository.findByTrainerSummary_UsernameAndYearAndMonth(any(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        // when
        trainerWorkloadService.updateWorkload(dto, ActionType.ADD);

        // then
        ArgumentCaptor<TrainerMonthlyWorkload> trainerCaptor =
                ArgumentCaptor.forClass(TrainerMonthlyWorkload.class);

        verify(trainerWorkloadRepository).save(trainerCaptor.capture());

        TrainerMonthlyWorkload savedTrainer = trainerCaptor.getValue();

        assertEquals("New.Trainer", savedTrainer.getUsername());
        assertEquals("New", savedTrainer.getFirstName());
        assertEquals("Trainer", savedTrainer.getLastName());
        assertTrue(savedTrainer.getActive());

        verify(monthlyWorkloadRepository).save(any(MonthlyWorkload.class));
    }

    @Test
    void deleteWorkload_existingTrainer_existingMonth() {
        // given
        TrainerMonthlyWorkload trainer = new TrainerMonthlyWorkload();
        trainer.setUsername("Juan.Perez");

        MonthlyWorkload workload = new MonthlyWorkload();
        workload.setTotalWorkload(120);

        TrainerWorkloadCommandMessageDto dto =
                new TrainerWorkloadCommandMessageDto(
                        "Juan.Perez",
                        "Juan",
                        "Perez",
                        true,
                        LocalDate.of(2025, 12, 5),

                        100
                );

        when(trainerWorkloadRepository.findByUsername("Juan.Perez"))
                .thenReturn(Optional.of(trainer));

        when(monthlyWorkloadRepository
                .findByTrainerSummary_UsernameAndYearAndMonth(trainer.getUsername(), 2025, 12))
                .thenReturn(Optional.of(workload));

        // when
        trainerWorkloadService.updateWorkload(dto, ActionType.DELETE);

        // then
        assertEquals(20, workload.getTotalWorkload());
        verify(monthlyWorkloadRepository).save(workload);
    }

    @Test
    void updateWorkload_delete_removesWorkload_whenResultIsZeroOrNegative(){
        // given
        TrainerMonthlyWorkload trainer = new TrainerMonthlyWorkload();
        trainer.setId(1L);
        trainer.setUsername("Trainer.One");

        MonthlyWorkload workload = new MonthlyWorkload();
        workload.setYear(2025);
        workload.setMonth(12);
        workload.setTotalWorkload(60);
        workload.setTrainerSummary(trainer);

        TrainerWorkloadCommandMessageDto dto = new TrainerWorkloadCommandMessageDto(
                "Trainer.One",
                "Trainer",
                "One",
                true,
                LocalDate.of(2025, 12, 5),
                60
        );

        when(trainerWorkloadRepository.findByUsername("Trainer.One"))
                .thenReturn(Optional.of(trainer));

        when(monthlyWorkloadRepository.findByTrainerSummary_UsernameAndYearAndMonth(
                trainer.getUsername(), 2025, 12
        )).thenReturn(Optional.of(workload));

        // when
        trainerWorkloadService.updateWorkload(dto, ActionType.DELETE);

        // then
        verify(monthlyWorkloadRepository).delete(workload);
        verify(monthlyWorkloadRepository, never()).save(any());
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

        assertThrows(IllegalArgumentException.class,
                () -> trainerWorkloadService.updateWorkload(request, null));

        // No workload operations should occur
        verify(monthlyWorkloadRepository, never()).save(any());
        verify(monthlyWorkloadRepository, never()).delete(any());
    }

    @Test
    void getWorkload_existingTrainer_returnsAggregatedData() {
        // given
        TrainerMonthlyWorkload trainer = new TrainerMonthlyWorkload();
        trainer.setId(1L);
        trainer.setUsername("Juan.Perez");
        trainer.setFirstName("Juan");
        trainer.setLastName("Perez");
        trainer.setActive(true);

        MonthlyWorkload workload = new MonthlyWorkload();
        workload.setTrainerSummary(trainer);
        workload.setYear(2025);
        workload.setMonth(12);
        workload.setTotalWorkload(240);

        when(trainerWorkloadRepository.findByUsername("Juan.Perez"))
                .thenReturn(Optional.of(trainer));

        when(monthlyWorkloadRepository.findByTrainerSummary_Username("Juan.Perez"))
                .thenReturn(List.of(workload));

        // when
        TrainerWorkloadResponseDto result =
                trainerWorkloadService.getTrainerWorkload("Juan.Perez", null, null);

        // then
        assertEquals("Juan.Perez", result.username());
        assertEquals("Juan", result.firstName());
        assertEquals("Perez", result.lastName());
        assertTrue(result.isActive());

        assertEquals(1, result.yearSummaryDtoList().size());

        YearSummaryDto yearSummary = result.yearSummaryDtoList().get(0);
        assertEquals(2025, yearSummary.year());

        assertEquals(1, yearSummary.monthSummaryDtoList().size());

        MonthSummaryDto monthSummary = yearSummary.monthSummaryDtoList().get(0);
        assertEquals(12, monthSummary.month());
        assertEquals(240, monthSummary.totalWorkload());
    }
}
