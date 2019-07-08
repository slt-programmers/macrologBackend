package csl.servicetests;

import csl.Application;
import csl.dto.AuthenticationRequest;
import csl.notification.MailService;
import csl.rest.ActivityService;
import csl.rest.AuthenticationService;
import csl.security.SecurityConstants;
import csl.security.ThreadLocalHolder;
import csl.security.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;


@Slf4j
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

    protected Integer createUser(String userEmail)  {
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder().email(userEmail).password("testpassword").username(userEmail).build();
        ResponseEntity responseEntity = authenticationService.signUp(authenticationRequest);
        Assert.isTrue(202 == responseEntity.getStatusCodeValue());
        return getUserIdFromResponseHeaderJWT(responseEntity);
    }

    protected Integer getUserIdFromResponseHeaderJWT( ResponseEntity responseEntity) {
        String jwtToken =responseEntity.getHeaders().get("token").get(0);
        Jws<Claims> claims = getClaimsJws(jwtToken);
        Integer userId = (Integer) claims.getBody().get("userId");
        log.debug("User id = " + userId);
        Assert.notNull(userId, "Geen UserID te herleiden");
        return userId;
    }

    protected void setUserContextFromJWTResponseHeader(ResponseEntity responseEntity){
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(Integer.valueOf(getUserIdFromResponseHeaderJWT(responseEntity)));
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    protected Jws<Claims> getClaimsJws(String jwtToken) {
        try {
            return Jwts.parser()
                    .setSigningKey(SecurityConstants.SECRET.getBytes("UTF-8"))
                    .parseClaimsJws(jwtToken);
        } catch (UnsupportedEncodingException uoe) {
            throw new RuntimeException("Unabled to read token");
        }
    }
}
