package com.jsalva.trainerworkload.service;

import com.jsalva.trainerworkload.dto.request.TrainerWorkloadRequestDto;
import com.jsalva.trainerworkload.dto.response.MonthSummaryDto;
import com.jsalva.trainerworkload.dto.response.TrainerWorkloadResponseDto;
import com.jsalva.trainerworkload.dto.response.YearSummaryDto;
import com.jsalva.trainerworkload.entity.ActionType;
import com.jsalva.trainerworkload.entity.MonthlyWorkload;
import com.jsalva.trainerworkload.entity.TrainerSummary;
import com.jsalva.trainerworkload.repository.MonthlyWorkloadRepository;
import com.jsalva.trainerworkload.repository.TrainerSummaryRepository;
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
    private TrainerSummaryRepository trainerSummaryRepository;

    @Mock
    private MonthlyWorkloadRepository monthlyWorkloadRepository;

    @InjectMocks
    private TrainerWorkloadServiceImpl trainerWorkloadService;

    @Test
    void addWorkload_existingTrainer_existingMonth() {
        // given
        TrainerSummary trainer = new TrainerSummary();
        trainer.setUsername("Juan.Perez");

        MonthlyWorkload workload = new MonthlyWorkload();
        workload.setTotalWorkload(120);

        TrainerWorkloadRequestDto dto =
                new TrainerWorkloadRequestDto(
                        "Juan.Perez",
                        "Juan",
                        "Perez",
                        true,
                        LocalDate.now(),

                        100,
                        ActionType.ADD
                );

        when(trainerSummaryRepository.findByUsername("Juan.Perez"))
                .thenReturn(Optional.of(trainer));

        when(monthlyWorkloadRepository
                .findByTrainerSummary_UsernameAndYearAndMonth(trainer.getUsername(), 2025, 12))
                .thenReturn(Optional.of(workload));

        // when
        trainerWorkloadService.updateWorkload(dto);

        // then
        assertEquals(220, workload.getTotalWorkload());
        verify(monthlyWorkloadRepository).save(workload);
    }

    @Test
    void addWorkload_newTrainer_existingMonth() {
        // given
        TrainerWorkloadRequestDto dto =
                new TrainerWorkloadRequestDto(
                        "New.Trainer",
                        "New",
                        "Trainer",
                        true,
                        LocalDate.now(),

                        100,
                        ActionType.ADD
                );

        when(trainerSummaryRepository.findByUsername("New.Trainer"))
                .thenReturn(Optional.empty());

        when(trainerSummaryRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(monthlyWorkloadRepository.findByTrainerSummary_UsernameAndYearAndMonth(any(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        // when
        trainerWorkloadService.updateWorkload(dto);

        // then
        ArgumentCaptor<TrainerSummary> trainerCaptor =
                ArgumentCaptor.forClass(TrainerSummary.class);

        verify(trainerSummaryRepository).save(trainerCaptor.capture());

        TrainerSummary savedTrainer = trainerCaptor.getValue();

        assertEquals("New.Trainer", savedTrainer.getUsername());
        assertEquals("New", savedTrainer.getFirstName());
        assertEquals("Trainer", savedTrainer.getLastName());
        assertTrue(savedTrainer.getActive());

        verify(monthlyWorkloadRepository).save(any(MonthlyWorkload.class));
    }

    @Test
    void deleteWorkload_existingTrainer_existingMonth() {
        // given
        TrainerSummary trainer = new TrainerSummary();
        trainer.setUsername("Juan.Perez");

        MonthlyWorkload workload = new MonthlyWorkload();
        workload.setTotalWorkload(120);

        TrainerWorkloadRequestDto dto =
                new TrainerWorkloadRequestDto(
                        "Juan.Perez",
                        "Juan",
                        "Perez",
                        true,
                        LocalDate.of(2025, 12, 5),

                        100,
                        ActionType.DELETE
                );

        when(trainerSummaryRepository.findByUsername("Juan.Perez"))
                .thenReturn(Optional.of(trainer));

        when(monthlyWorkloadRepository
                .findByTrainerSummary_UsernameAndYearAndMonth(trainer.getUsername(), 2025, 12))
                .thenReturn(Optional.of(workload));

        // when
        trainerWorkloadService.updateWorkload(dto);

        // then
        assertEquals(20, workload.getTotalWorkload());
        verify(monthlyWorkloadRepository).save(workload);
    }

    @Test
    void updateWorkload_delete_removesWorkload_whenResultIsZeroOrNegative(){
        // given
        TrainerSummary trainer = new TrainerSummary();
        trainer.setId(1L);
        trainer.setUsername("Trainer.One");

        MonthlyWorkload workload = new MonthlyWorkload();
        workload.setYear(2025);
        workload.setMonth(12);
        workload.setTotalWorkload(60);
        workload.setTrainerSummary(trainer);

        TrainerWorkloadRequestDto dto = new TrainerWorkloadRequestDto(
                "Trainer.One",
                "Trainer",
                "One",
                true,
                LocalDate.of(2025, 12, 5),
                60,
                ActionType.DELETE
        );

        when(trainerSummaryRepository.findByUsername("Trainer.One"))
                .thenReturn(Optional.of(trainer));

        when(monthlyWorkloadRepository.findByTrainerSummary_UsernameAndYearAndMonth(
                trainer.getUsername(), 2025, 12
        )).thenReturn(Optional.of(workload));

        // when
        trainerWorkloadService.updateWorkload(dto);

        // then
        verify(monthlyWorkloadRepository).delete(workload);
        verify(monthlyWorkloadRepository, never()).save(any());
    }

    @Test
    void updateWorkload_nullAction_throwsException() {
        TrainerWorkloadRequestDto request = new TrainerWorkloadRequestDto(
                "Trainer.One",
                "Trainer",
                "One",
                true,
                LocalDate.of(2025, 12, 5),
                60,
                null
        );

        assertThrows(IllegalArgumentException.class,
                () -> trainerWorkloadService.updateWorkload(request));

        // No workload operations should occur
        verify(monthlyWorkloadRepository, never()).save(any());
        verify(monthlyWorkloadRepository, never()).delete(any());
    }

    @Test
    void getWorkload_existingTrainer_returnsAggregatedData() {
        // given
        TrainerSummary trainer = new TrainerSummary();
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

        when(trainerSummaryRepository.findByUsername("Juan.Perez"))
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
