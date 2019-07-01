package csl.servicetests;

import csl.Application;
import csl.notification.MailService;
import csl.rest.ActivityService;
import csl.rest.AuthenticationService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {Application.class, IntegrationConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.application.version=1.0",
                "actievePus=ZTW"
        })
public abstract class AbstractApplicationIntegrationTest {

    @Autowired
    protected MailService mailService;

    @Autowired
    protected ActivityService activityService;

    @Autowired
    protected AuthenticationService authenticationService;

}
