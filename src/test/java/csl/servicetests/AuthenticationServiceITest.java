package csl.servicetests;

import csl.dto.AuthenticationRequest;
import csl.dto.ChangePasswordRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
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
    public void testSignupUserOrEmailAlreadyKnown() throws InterruptedException {

        String userName = "userknown";
        String userEmail = "emailknown@test.example";

        // 1e: keer aanmaken succesvol:
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder().email(userEmail).password("testpassword").username(userName).build();
        ResponseEntity responseEntity = authenticationService.signUp(authenticationRequest);
        Assertions.assertEquals(202, responseEntity.getStatusCodeValue());

        boolean mailSend = ((MyMockedMailService) mailService).verifyConfirmationSendTo(userEmail);
        int cnt=0;
        while(!mailSend && cnt < 10) {
            Thread.sleep(1000);
            mailSend = ((MyMockedMailService) mailService).verifyConfirmationSendTo(userEmail);
            cnt++;
            log.debug("Sleep until we fix better thread waiting.");
        }
        Assertions.assertTrue(mailSend,"Mail has been send");

        // 2e: keer afgekeurd op username
        authenticationRequest = AuthenticationRequest.builder().email("diffemail@email.com").password("testpassword").username(userName).build();
        responseEntity = authenticationService.signUp(authenticationRequest);
        Assertions.assertEquals(401, responseEntity.getStatusCodeValue());
        mailSend = ((MyMockedMailService) mailService).verifyConfirmationSendTo(userEmail);
        Assertions.assertFalse(mailSend,"Mail should not be send");

        // 3e: keer afgekeurd op email
        authenticationRequest = AuthenticationRequest.builder().email(userEmail).password("testpassword").username("diffusername").build();
        responseEntity = authenticationService.signUp(authenticationRequest);
        Assertions.assertEquals(401, responseEntity.getStatusCodeValue());
        mailSend = ((MyMockedMailService) mailService).verifyConfirmationSendTo(userEmail);
        Assertions.assertFalse(mailSend,"Mail should not be send");

    }

    @Test
    public void testResetPassword() throws InterruptedException {

        String userName = "userResetPassword";
        String userEmail = "userResetPassword@test.example";
        String password = "password1";

        // 1e: keer aanmaken succesvol:
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder().email(userEmail).password(password).username(userName).build();
        ResponseEntity responseEntity = authenticationService.signUp(authenticationRequest);
        Assertions.assertEquals(202, responseEntity.getStatusCodeValue());

        setUserContextFromJWTResponseHeader(responseEntity);

        // 1e keer inloggen met wachtwoord
        authenticationRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authenticationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED.value(), responseEntity.getStatusCodeValue());

        // wachtwoord resetten
        AuthenticationRequest changePasswordRequest = AuthenticationRequest.builder().email(userEmail).build();
        responseEntity = authenticationService.resetPassword(changePasswordRequest);
        Assertions.assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());

        // inloggen met oude kan nog:
        authenticationRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authenticationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED.value(), responseEntity.getStatusCodeValue());

        String resettedPassword = ((MyMockedMailService) mailService).getResettedPassword(userEmail);
        int cnt=0;
        while(resettedPassword == null && cnt < 10) {
            Thread.sleep(1000);
            resettedPassword = ((MyMockedMailService) mailService).getResettedPassword(userEmail);
            cnt++;
            log.debug("Sleep until we fix better thread waiting.");
        }
        Assertions.assertTrue(resettedPassword != null,"Mail has been send");

        log.debug("Reset to " + resettedPassword);
        // inloggen met nieuw wachtwoord
        authenticationRequest = AuthenticationRequest.builder().username(userEmail).password(resettedPassword).build();
        responseEntity = authenticationService.authenticateUser(authenticationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED.value(), responseEntity.getStatusCodeValue());

        // inloggen met oude kan niet meer :
        authenticationRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authenticationRequest);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), responseEntity.getStatusCodeValue());

    }


    @Test
    public void testChangePassword() {

        String userName = "userChangePassword";
        String userEmail = "userChangePassword@test.example";
        String password = "password1";
        String newPassword = "password2";

        // 1e: keer aanmaken succesvol:
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder().email(userEmail).password(password).username(userName).build();
        ResponseEntity responseEntity = authenticationService.signUp(authenticationRequest);
        Assertions.assertEquals(202, responseEntity.getStatusCodeValue());

        setUserContextFromJWTResponseHeader(responseEntity);

        // 1e keer inloggen met wachtwoord
        authenticationRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authenticationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED.value(), responseEntity.getStatusCodeValue());

        // wachtwoord veranderen
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder().oldPassword(password).newPassword(newPassword).confirmPassword(newPassword).build();
        responseEntity = authenticationService.changePassword(changePasswordRequest);
        Assertions.assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());

        // inloggen met oude kan niet meer:
        authenticationRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authenticationRequest);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), responseEntity.getStatusCodeValue());

        // inloggen met nieuwe kan wel:
        authenticationRequest = AuthenticationRequest.builder().username(userEmail).password(newPassword).build();
        responseEntity = authenticationService.authenticateUser(authenticationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED.value(), responseEntity.getStatusCodeValue());


    }

}
