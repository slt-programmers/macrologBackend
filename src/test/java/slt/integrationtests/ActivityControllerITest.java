package slt.integrationtests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;

import slt.dto.ActivityDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.integrationtests.utils.AbstractApplicationIntegrationTest;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ActivityControllerITest extends AbstractApplicationIntegrationTest {

    private Long userId;

    @BeforeEach
    public void setUserContext() {
        if (this.userId == null) {
            log.debug("Creating test user for test {}", this.getClass().getName());
            this.userId = createUser(this.getClass().getName());
        }
        final var userInfo = UserInfo.builder().userId(this.userId).build();
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    public void testActivities() {
        final var newActivities = Arrays.asList(
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
        final var responseEntity = activityController.postActivities("2001-01-01", newActivities);

        // Check response object from store call
        final var newEntries = responseEntity.getBody();
        Assertions.assertNotNull(newEntries);
        assertEquals(2, newEntries.size());

       final var running = newEntries.stream().filter(a -> a.getName().equals("Running")).findFirst();
        assertTrue(running.isPresent(), "Running");

        assertEquals(20.0, running.get().getCalories().doubleValue());
        assertEquals("Running", running.get().getName());
        assertTrue(isEqualDate(running.get().getDay(), LocalDate.parse("2001-01-01")));
        Assertions.assertNotNull(running.get().getId());

        final var cycling = newEntries.stream().filter(a -> a.getName().equals("Cycling")).findFirst();
        assertTrue(cycling.isPresent(), "Cycling");

        assertEquals(30.0, cycling.get().getCalories().doubleValue());
        assertEquals("Cycling", cycling.get().getName());
        assertTrue(isEqualDate(cycling.get().getDay(), LocalDate.parse("2001-01-01")));
        Assertions.assertNotNull(cycling.get().getId());

        // Check response object from get activities for day
        final var activitiesForDay = activityController.getActivitiesForDay("2001-01-01", false);
        assertEquals(HttpStatus.OK, activitiesForDay.getStatusCode());

        final var newResponseEntries = activitiesForDay.getBody();
        Assertions.assertNotNull(newResponseEntries);
        assertEquals(2, newResponseEntries.size());

        final var runningResponse = newResponseEntries.stream().filter(a -> a.getName().equals("Running")).findFirst();
        assertTrue(runningResponse.isPresent(), "Running");

        assertEquals(20.0, runningResponse.get().getCalories().doubleValue());
        assertEquals("Running", runningResponse.get().getName());
        assertTrue(isEqualDate(runningResponse.get().getDay(), LocalDate.parse("2001-01-01")));
        Assertions.assertNotNull(runningResponse.get().getId());

        final var cyclingResponse = newResponseEntries.stream().filter(a -> a.getName().equals("Cycling")).findFirst();
        assertTrue(cyclingResponse.isPresent(), "Cycling");

        assertEquals(30.0, cyclingResponse.get().getCalories().doubleValue());
        assertEquals("Cycling", cyclingResponse.get().getName());
        assertTrue(isEqualDate(cyclingResponse.get().getDay(), LocalDate.parse("2001-01-01")));
        Assertions.assertNotNull(cyclingResponse.get().getId());

        // delete 1 activity. remove cycling
        activityController.deleteActivity(cyclingResponse.get().getId());
        assertEquals(HttpStatus.OK, activitiesForDay.getStatusCode());

        // Check response object from get activities for day.
        final var getActivitiesResponse = activityController.getActivitiesForDay("2001-01-01", false);
        assertEquals(HttpStatus.OK, getActivitiesResponse.getStatusCode());

        final var newResponseEntries1 = getActivitiesResponse.getBody();
        Assertions.assertNotNull(newResponseEntries1);
        assertEquals(1, newResponseEntries1.size());

        final var runningResponse2 = newResponseEntries1.stream().filter(a -> a.getName().equals("Running")).findFirst();
        assertTrue(runningResponse2.isPresent(), "Running");

        // Update Running
        final var running3 = runningResponse2.get();
        final var updatedRunning = ActivityDto.builder()
                .id(running3.getId())
                .calories(44.0)
                .syncedId(running3.getSyncedId())
                .day(running3.getDay())
                .name(running3.getName())
                .syncedWith(running3.getSyncedWith())
                .build();
        final var responseEntity1 = activityController.postActivities("2001-01-01", List.of(updatedRunning));
        assertEquals(HttpStatus.OK, responseEntity1.getStatusCode());
        final var newResponseEntries2 = responseEntity1.getBody();
        Assertions.assertNotNull(newResponseEntries2);
        assertEquals(1, newResponseEntries2.size());

        final var activitiesForDay1 = activityController.getActivitiesForDay("2001-01-01", false);
        assertEquals(HttpStatus.OK, activitiesForDay1.getStatusCode());
        final var newResponseEntries3 = activitiesForDay1.getBody();
        Assertions.assertNotNull(newResponseEntries3);
        assertEquals(1, newResponseEntries3.size());
        final var runningResponse3 = newResponseEntries3.stream().filter(a -> a.getName().equals("Running")).findFirst();
        assertTrue(runningResponse3.isPresent(), "Running");
        assertEquals(44.0, runningResponse3.get().getCalories().doubleValue());
    }
}
