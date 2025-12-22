package slt.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import slt.database.entities.LogActivity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
class ActivityRepositoryTest {

    @Mock
    ActivityCrudRepository activityCrudRepository;

    @InjectMocks
    ActivityRepository activityRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void deleteLogActivityNotFound() {

        when(activityCrudRepository.findById(eq(2L))).thenReturn(Optional.empty());

        activityRepository.deleteLogActivity(1L,2L);

        verify(activityCrudRepository).findById(eq(2L));
        verifyNoMoreInteractions(activityCrudRepository);

    }

    @Test
    void deleteLogActivityFound() {

        when(activityCrudRepository.findById(eq(2L))).thenReturn(Optional.of(LogActivity.builder().build()));

        activityRepository.deleteLogActivity(1L,2L);

        verify(activityCrudRepository).findById(eq(2L));
        verify(activityCrudRepository).deleteByUserIdAndId(eq(1L), eq(2L));
        verifyNoMoreInteractions(activityCrudRepository);

    }

    @Test
    void deleteLogActivityFoundSynced() {

        when(activityCrudRepository.findById(eq(2L)))
                .thenReturn(Optional.of(LogActivity.builder().syncedWith("STRAVA").build()));

        activityRepository.deleteLogActivity(1L,2L);

        ArgumentCaptor<LogActivity> savedActitivty = ArgumentCaptor.forClass(LogActivity.class);

        verify(activityCrudRepository).findById(eq(2L));
        verify(activityCrudRepository).save(savedActitivty.capture());

        assertThat(savedActitivty.getValue().getStatus()).isEqualTo("DELETED");
        verifyNoMoreInteractions(activityCrudRepository);

    }

    @Test
    void countByUserIdAndSyncedWith(){
        activityCrudRepository.countByUserIdAndSyncedWith(1L,"s");
        verify(activityCrudRepository).countByUserIdAndSyncedWith(
                eq(1L ),eq("s") );
        verifyNoMoreInteractions(activityCrudRepository);
    }

    @Test
    void findByUserIdAndSyncedWithAndSyncedId(){
        activityCrudRepository.findByUserIdAndSyncedWithAndSyncedId(1L,"s",1L);
        verify(activityCrudRepository).findByUserIdAndSyncedWithAndSyncedId(
                eq(1L ),eq("s") ,eq(1L));
        verifyNoMoreInteractions(activityCrudRepository);
    }

}