package slt.integrationtests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import slt.dto.ActivityDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.integrationtests.utils.AbstractApplicationIntegrationTest;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ActivityControllerITest extends AbstractApplicationIntegrationTest {

    private Long userId;

    @BeforeEach
    public void setUserContext() {
        if (this.userId == null) {
            log.debug("Creating test user for test " + this.getClass().getName());
            this.userId = createUser(this.getClass().getName());
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(this.userId);
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    public void testActivities() {
        List<ActivityDto> newActivities = Arrays.asList(
                ActivityDto.builder()
                        .day(Date.valueOf(LocalDate.parse("2001-01-01")))
                        .name("Running")
                        .calories(20.0)
                        .build(),
                ActivityDto.builder()
                        .day(Date.valueOf(LocalDate.parse("2001-01-01")))
                        .name("Cycling")
                        .calories(30.0)
                        .build()

        );
        ResponseEntity<List<ActivityDto>> responseEntity = activityController.postActivities("2001-01-01", newActivities);

        // Check response object from store call
        List<ActivityDto> newEntries = responseEntity.getBody();
        assert newEntries != null;
        assertEquals(2, newEntries.size());

        Optional<ActivityDto> running = newEntries.stream().filter(a -> a.getName().equals("Running")).findFirst();
        assertTrue(running.isPresent(), "Running");

        assertEquals(20.0, running.get().getCalories().doubleValue());
        assertEquals("Running", running.get().getName());
        assertTrue(isEqualDate(running.get().getDay(), LocalDate.parse("2001-01-01")));
        Assertions.assertNotNull(running.get().getId());

        Optional<ActivityDto> cycling = newEntries.stream().filter(a -> a.getName().equals("Cycling")).findFirst();
        assertTrue(cycling.isPresent(), "Cycling");

        assertEquals(30.0, cycling.get().getCalories().doubleValue());
        assertEquals("Cycling", cycling.get().getName());
        assertTrue(isEqualDate(cycling.get().getDay(), LocalDate.parse("2001-01-01")));
        Assertions.assertNotNull(cycling.get().getId());

        // Check response object from get activities for day
        ResponseEntity<List<ActivityDto>> activitiesForDay = activityController.getActivitiesForDay("2001-01-01", false);
        assertEquals(HttpStatus.OK, activitiesForDay.getStatusCode());

        List<ActivityDto> newResponseEntries = activitiesForDay.getBody();
        assert newResponseEntries != null;
        assertEquals(2, newResponseEntries.size());

        Optional<ActivityDto> runningResponse = newResponseEntries.stream().filter(a -> a.getName().equals("Running")).findFirst();
        assertTrue(runningResponse.isPresent(), "Running");

        assertEquals(20.0, runningResponse.get().getCalories().doubleValue());
        assertEquals("Running", runningResponse.get().getName());
        assertTrue(isEqualDate(runningResponse.get().getDay(), LocalDate.parse("2001-01-01")));
        Assertions.assertNotNull(runningResponse.get().getId());

        Optional<ActivityDto> cyclingResponse = newResponseEntries.stream().filter(a -> a.getName().equals("Cycling")).findFirst();
        assertTrue(cyclingResponse.isPresent(), "Cycling");

        assertEquals(30.0, cyclingResponse.get().getCalories().doubleValue());
        assertEquals("Cycling", cyclingResponse.get().getName());
        assertTrue(isEqualDate(cyclingResponse.get().getDay(), LocalDate.parse("2001-01-01")));
        Assertions.assertNotNull(cyclingResponse.get().getId());

        // delete 1 activity. remove cycling
        activityController.deleteActivity(cyclingResponse.get().getId());
        assertEquals(HttpStatus.OK, activitiesForDay.getStatusCode());

        // Check response object from get activities for day.
        activitiesForDay = activityController.getActivitiesForDay("2001-01-01", false);
        assertEquals(HttpStatus.OK, activitiesForDay.getStatusCode());

        newResponseEntries = activitiesForDay.getBody();
        assert newResponseEntries != null;
        assertEquals(1, newResponseEntries.size());

        runningResponse = newResponseEntries.stream().filter(a -> a.getName().equals("Running")).findFirst();
        assertTrue(runningResponse.isPresent(), "Running");

        // Update Running
        runningResponse.get().setCalories(44.0);
        responseEntity = activityController.postActivities("2001-01-01", Collections.singletonList(runningResponse.get()));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        newResponseEntries = responseEntity.getBody();
        assert newResponseEntries != null;
        assertEquals(1, newResponseEntries.size());

        activitiesForDay = activityController.getActivitiesForDay("2001-01-01", false);
        assertEquals(HttpStatus.OK, activitiesForDay.getStatusCode());
        newResponseEntries = activitiesForDay.getBody();
        assert newResponseEntries != null;
        assertEquals(1, newResponseEntries.size());
        runningResponse = newResponseEntries.stream().filter(a -> a.getName().equals("Running")).findFirst();
        assertTrue(runningResponse.isPresent(), "Running");
        assertEquals(44.0, runningResponse.get().getCalories().doubleValue());

    }
}
