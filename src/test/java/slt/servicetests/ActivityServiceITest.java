package slt.servicetests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import slt.dto.LogActivityDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.servicetests.utils.AbstractApplicationIntegrationTest;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ActivityServiceITest extends AbstractApplicationIntegrationTest {

    private Integer userId;

    @BeforeEach
    public void setUserContext() {

        if (this.userId == null ) {
            log.debug("Creating test user for test " + this.getClass().getName());
            this.userId = createUser(this.getClass().getName());
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(Integer.valueOf(this.userId));
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    public void testActivities() {

        List<LogActivityDto> newActivities = Arrays.asList(
                LogActivityDto.builder()
                        .day(Date.valueOf(LocalDate.parse("2001-01-01" )))
                        .name("Running")
                        .calories(20.0)
                        .build(),
                LogActivityDto.builder()
                        .day(Date.valueOf(LocalDate.parse("2001-01-01" )))
                        .name("Cycling")
                        .calories(30.0)
                        .build()

        );
        ResponseEntity responseEntity = activityService.storeActivities(newActivities);

        // Check response object from store call
        List<LogActivityDto> newEntries = (List<LogActivityDto>) responseEntity.getBody();
        assertEquals(2, newEntries.size());

        Optional<LogActivityDto> running = newEntries.stream().filter(a -> a.getName().equals("Running")).findFirst();
        assertTrue(running.isPresent(),"Running");

        assertEquals(20.0, running.get().getCalories());
        assertEquals("Running", running.get().getName());
        Assertions.assertNotNull(isEqualDate(running.get().getDay(),LocalDate.parse("2001-01-01" )));
        Assertions.assertNotNull(running.get().getId());

        Optional<LogActivityDto> cycling = newEntries.stream().filter(a -> a.getName().equals("Cycling")).findFirst();
        assertTrue(cycling.isPresent(),"Cycling");

        assertEquals(30.0, cycling.get().getCalories());
        assertEquals("Cycling", cycling.get().getName());
        Assertions.assertNotNull(isEqualDate(cycling.get().getDay(),LocalDate.parse("2001-01-01" )));
        Assertions.assertNotNull(cycling.get().getId());

        // Check response object from get activities for day
        ResponseEntity activitiesForDay = activityService.getActivitiesForDay("2001-01-01");
        Assert.isTrue(200 == activitiesForDay.getStatusCodeValue());

        List<LogActivityDto> newResponseEntries= (List<LogActivityDto>) activitiesForDay.getBody();
        assertEquals(newResponseEntries.size(),2);

        Optional<LogActivityDto> runningResponse = newResponseEntries.stream().filter(a -> a.getName().equals("Running")).findFirst();
        assertTrue(runningResponse.isPresent(),"Running");

        assertEquals(20.0, runningResponse.get().getCalories());
        assertEquals("Running", runningResponse.get().getName());
        Assertions.assertNotNull(isEqualDate(runningResponse.get().getDay(),LocalDate.parse("2001-01-01" )));
        Assertions.assertNotNull(runningResponse.get().getId());

        Optional<LogActivityDto> cyclingResponse = newResponseEntries.stream().filter(a -> a.getName().equals("Cycling")).findFirst();
        assertTrue(cyclingResponse.isPresent(),"Cycling");

        assertEquals(30.0, cyclingResponse.get().getCalories());
        assertEquals("Cycling", cyclingResponse.get().getName());
        Assertions.assertNotNull(isEqualDate(cyclingResponse.get().getDay(),LocalDate.parse("2001-01-01" )));
        Assertions.assertNotNull(cyclingResponse.get().getId());

        // delete 1 activity. remove cycling
        ResponseEntity responseEntity1 = activityService.deleteActivity(cyclingResponse.get().getId());
        Assert.isTrue(200 == activitiesForDay.getStatusCodeValue());

        // Check response object from get activities for day.
        activitiesForDay = activityService.getActivitiesForDay("2001-01-01");
        Assert.isTrue(200 == activitiesForDay.getStatusCodeValue());

        newResponseEntries= (List<LogActivityDto>) activitiesForDay.getBody();
        assertEquals(newResponseEntries.size(),1);

        runningResponse = newResponseEntries.stream().filter(a -> a.getName().equals("Running")).findFirst();
        assertTrue(runningResponse.isPresent(),"Running");

        // Update Running
        runningResponse.get().setCalories(44.0);
        responseEntity = activityService.storeActivities(Arrays.asList(runningResponse.get()));
        Assert.isTrue(200 == responseEntity.getStatusCodeValue());
        newResponseEntries= (List<LogActivityDto>) responseEntity.getBody();
        assertEquals(newResponseEntries.size(),1);

        activitiesForDay = activityService.getActivitiesForDay("2001-01-01");
        Assert.isTrue(200 == activitiesForDay.getStatusCodeValue());
        newResponseEntries= (List<LogActivityDto>) activitiesForDay.getBody();
        assertEquals(newResponseEntries.size(),1);
        runningResponse = newResponseEntries.stream().filter(a -> a.getName().equals("Running")).findFirst();
        assertTrue(runningResponse.isPresent(),"Running");
        assertTrue(runningResponse.get().getCalories().equals(44.0), "Calorien bijgewerkt");



    }
}
