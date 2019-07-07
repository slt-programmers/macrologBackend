package csl.servicetests;

import csl.dto.LogActivityDto;
import csl.security.ThreadLocalHolder;
import csl.security.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ActivityServiceITest extends AbstractApplicationIntegrationTest {

    private Integer userId;

    @BeforeEach
    public void setUserContext() throws UnsupportedEncodingException {

        if (this.userId == null ) {
            log.debug("Creating test user for test " + this.getClass());
            Integer activityUser = createUser("activityUser");
            this.userId = activityUser;
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(Integer.valueOf(this.userId));
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    public void testCreateActivities() {



        List<LogActivityDto> newActivities = Arrays.asList(
                LogActivityDto.builder()
                        .day(Date.valueOf(LocalDate.parse("2001-01-01" )))
                        .name("Running")
                        .calories(20.0)
                        .build()
        );
        ResponseEntity responseEntity = activityService.storeActivities(newActivities);
        List<LogActivityDto> newEntries = (List<LogActivityDto>) responseEntity.getBody();

        Assertions.assertEquals(1, newEntries.size());
        Assertions.assertEquals(20.0, newEntries.get(0).getCalories());
        Assertions.assertEquals("Running", newEntries.get(0).getName());
        Assertions.assertNotNull(newEntries.get(0).getDay());
        Assertions.assertNotNull(newEntries.get(0).getId());


        ResponseEntity activitiesForDay = activityService.getActivitiesForDay("01-01-2001");
        Assert.isTrue(200 == activitiesForDay.getStatusCodeValue());
    }
}
