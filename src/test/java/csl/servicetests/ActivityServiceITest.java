package csl.servicetests;

import csl.security.ThreadLocalHolder;
import csl.security.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

@Slf4j
public class ActivityServiceITest extends AbstractApplicationIntegrationTest {

    @Test
    public void testCreateActivities() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(1);
        ThreadLocalHolder.getThreadLocal().set(userInfo);
        ResponseEntity activitiesForDay = activityService.getActivitiesForDay("01-01-2001");
        Assert.isTrue(200 == activitiesForDay.getStatusCodeValue());
    }
}
