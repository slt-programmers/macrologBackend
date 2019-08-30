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
    LogActivityCrudRepository logActivityCrudRepository;

    @InjectMocks
    ActivityRepository activityRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void deleteLogActivityNotFound() {

        when(logActivityCrudRepository.findById(eq(2L))).thenReturn(Optional.empty());

        activityRepository.deleteLogActivity(1,2L);

        verify(logActivityCrudRepository).findById(eq(2L));
        verifyNoMoreInteractions(logActivityCrudRepository);

    }

    @Test
    void deleteLogActivityFound() {

        when(logActivityCrudRepository.findById(eq(2L))).thenReturn(Optional.of(LogActivity.builder().build()));

        activityRepository.deleteLogActivity(1,2L);

        verify(logActivityCrudRepository).findById(eq(2L));
        verify(logActivityCrudRepository).deleteByUserIdAndId(eq(1), eq(2L));
        verifyNoMoreInteractions(logActivityCrudRepository);

    }

    @Test
    void deleteLogActivityFoundSynced() {

        when(logActivityCrudRepository.findById(eq(2L)))
                .thenReturn(Optional.of(LogActivity.builder().syncedWith("STRAVA").build()));

        activityRepository.deleteLogActivity(1,2L);

        ArgumentCaptor<LogActivity> savedActitivty = ArgumentCaptor.forClass(LogActivity.class);

        verify(logActivityCrudRepository).findById(eq(2L));
        verify(logActivityCrudRepository).save(savedActitivty.capture());

        assertThat(savedActitivty.getValue().getStatus()).isEqualTo("DELETED");
        verifyNoMoreInteractions(logActivityCrudRepository);

    }

    @Test
    void countByUserIdAndSyncedWith(){
        logActivityCrudRepository.countByUserIdAndSyncedWith(1,"s");
        verify(logActivityCrudRepository).countByUserIdAndSyncedWith(
                eq(1 ),eq("s") );
        verifyNoMoreInteractions(logActivityCrudRepository);
    }

    @Test
    void findByUserIdAndSyncedWithAndSyncedId(){
        logActivityCrudRepository.findByUserIdAndSyncedWithAndSyncedId(1,"s",1L);
        verify(logActivityCrudRepository).findByUserIdAndSyncedWithAndSyncedId(
                eq(1 ),eq("s") ,eq(1L));
        verifyNoMoreInteractions(logActivityCrudRepository);
    }

}