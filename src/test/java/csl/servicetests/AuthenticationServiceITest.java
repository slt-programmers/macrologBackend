package csl.servicetests;

import csl.dto.AuthenticationRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

@Slf4j
public class AuthenticationServiceITest extends AbstractApplicationIntegrationTest {


    @Test
    public void testSignupNewUser() {

        String userName = "newuser";
        String userEmail = "newuser@test.example";

        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder().email(userEmail).password("testpassword").username(userName).build();
        ResponseEntity responseEntity = authenticationService.signUp(authenticationRequest);
        Assertions.assertEquals(202, responseEntity.getStatusCodeValue());
        String jwtToken = responseEntity.getHeaders().get("token").get(0);
        log.debug(jwtToken);
        Jws<Claims> claimsJws = getClaimsJws(jwtToken);
        Integer userId = (Integer) claimsJws.getBody().get("userId");
        log.debug("User id = " + userId);

    }

    @Test
    public void testSignupUserOrEmailAlreadyKnown() {

        String userName = "userknown";
        String userEmail = "emailknown@test.example";

        // 1e: keer aanmaken succesvol:
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder().email(userEmail).password("testpassword").username(userName).build();
        ResponseEntity responseEntity = authenticationService.signUp(authenticationRequest);
        Assertions.assertEquals(202, responseEntity.getStatusCodeValue());

        // 2e: keer afgekeurd op username
        authenticationRequest = AuthenticationRequest.builder().email("diffemail@email.com").password("testpassword").username(userName).build();
        responseEntity = authenticationService.signUp(authenticationRequest);
        Assertions.assertEquals(401, responseEntity.getStatusCodeValue());

        // 3e: keer afgekeurd op email
        authenticationRequest = AuthenticationRequest.builder().email(userEmail).password("testpassword").username("diffusername").build();
        responseEntity = authenticationService.signUp(authenticationRequest);
        Assertions.assertEquals(401, responseEntity.getStatusCodeValue());

    }


}
