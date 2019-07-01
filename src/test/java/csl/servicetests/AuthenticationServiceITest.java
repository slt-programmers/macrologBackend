package csl.servicetests;

import csl.database.model.UserAccount;
import csl.dto.AuthenticationRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@Slf4j
public class AuthenticationServiceITest extends AbstractApplicationIntegrationTest {

    @Test
    public void createUserTest() {

        String userEmail = "itest@test.example";

        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder().email(userEmail).password("testpassword").username("itester").build();
        ResponseEntity responseEntity = authenticationService.signUp(authenticationRequest);
        Assert.isTrue(202 == responseEntity.getStatusCodeValue());
        String jwtToken =responseEntity.getHeaders().get("token").get(0);
        log.debug(jwtToken);

        Mockito.verify(mailService).sendConfirmationMail(eq(userEmail), any(UserAccount.class));
    }
}
