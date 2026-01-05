package slt.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
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
    void deleteActivity() {
        activityRepository.deleteActivity(1L, 2L);
        verify(activityCrudRepository).deleteByUserIdAndId(eq(1L), eq(2L));
    }

    @Test
    void countByUserIdAndSyncedWith() {
        activityCrudRepository.countByUserIdAndSyncedWith(1L, "s");
        verify(activityCrudRepository).countByUserIdAndSyncedWith(eq(1L), eq("s"));
        verifyNoMoreInteractions(activityCrudRepository);
    }

    @Test
    void findByUserIdAndSyncedWithAndSyncedId() {
        activityCrudRepository.findByUserIdAndSyncedWithAndSyncedId(1L, "s", 1L);
        verify(activityCrudRepository).findByUserIdAndSyncedWithAndSyncedId(eq(1L), eq("s"), eq(1L));
        verifyNoMoreInteractions(activityCrudRepository);
    }

}