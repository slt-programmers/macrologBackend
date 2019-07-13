package csl.servicetests;

import csl.dto.AuthenticationRequest;
import csl.dto.ChangePasswordRequest;
import csl.dto.RegistrationRequest;
import csl.dto.ResetPasswordRequest;
import csl.servicetests.utils.AbstractApplicationIntegrationTest;
import csl.servicetests.utils.MyMockedMailService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

@Slf4j
class AuthenticationServiceITest extends AbstractApplicationIntegrationTest {

    @Test
    void testSignupNewUser() {

        String userName = "newuser";
        String userEmail = "newuser@test.example";

        RegistrationRequest registrationRequest = RegistrationRequest.builder().email(userEmail).password("testpassword").username(userName).build();
        ResponseEntity responseEntity = authenticationService.signUp(registrationRequest);
        Assertions.assertEquals(202, responseEntity.getStatusCodeValue());
        String jwtToken = Objects.requireNonNull(responseEntity.getHeaders().get("token")).get(0);
        log.debug(jwtToken);
        Jws<Claims> claimsJws = getClaimsJws(jwtToken);
        Integer userId = (Integer) claimsJws.getBody().get("userId");
        log.debug("User id = " + userId);
    }

    @Test
    void testSignupUserOrEmailAlreadyKnown() throws InterruptedException {

        String userName = "userknown";
        String userEmail = "emailknown@test.example";

        // 1e: keer aanmaken succesvol:
        RegistrationRequest registrationRequest = RegistrationRequest.builder().email(userEmail).password("testpassword").username(userName).build();
        ResponseEntity responseEntity = authenticationService.signUp(registrationRequest);
        Assertions.assertEquals(202, responseEntity.getStatusCodeValue());

        boolean mailSend = ((MyMockedMailService) mailService).verifyConfirmationSendTo(userEmail);
        int cnt = 0;
        while (!mailSend && cnt < 10) {
            Thread.sleep(1000);
            mailSend = ((MyMockedMailService) mailService).verifyConfirmationSendTo(userEmail);
            cnt++;
            log.debug("Sleep until we fix better thread waiting.");
        }
        Assertions.assertTrue(mailSend, "Mail has been send");

        // 2e: keer afgekeurd op username
        registrationRequest = RegistrationRequest.builder().email("diffemail@email.com").password("testpassword").username(userName).build();
        responseEntity = authenticationService.signUp(registrationRequest);
        Assertions.assertEquals(401, responseEntity.getStatusCodeValue());
        mailSend = ((MyMockedMailService) mailService).verifyConfirmationSendTo(userEmail);
        Assertions.assertFalse(mailSend, "Mail should not be send");

        // 3e: keer afgekeurd op email
        registrationRequest = RegistrationRequest.builder().email(userEmail).password("testpassword").username("diffusername").build();
        responseEntity = authenticationService.signUp(registrationRequest);
        Assertions.assertEquals(401, responseEntity.getStatusCodeValue());
        mailSend = ((MyMockedMailService) mailService).verifyConfirmationSendTo(userEmail);
        Assertions.assertFalse(mailSend, "Mail should not be send");

    }

    @Test
    void testResetPassword() throws InterruptedException {

        String userName = "userResetPassword";
        String userEmail = "userResetPassword@test.example";
        String password = "password1";

        // 1e: keer aanmaken succesvol:
        RegistrationRequest registrationRequest = RegistrationRequest.builder().email(userEmail).password(password).username(userName).build();
        ResponseEntity responseEntity = authenticationService.signUp(registrationRequest);
        Assertions.assertEquals(202, responseEntity.getStatusCodeValue());

        setUserContextFromJWTResponseHeader(responseEntity);

        // 1e keer inloggen met wachtwoord
        AuthenticationRequest authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED.value(), responseEntity.getStatusCodeValue());

        // wachtwoord resetten
        ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder().email(userEmail).build();
        responseEntity = authenticationService.resetPassword(resetPasswordRequest);
        Assertions.assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());

        // inloggen met oude kan nog:
        authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED.value(), responseEntity.getStatusCodeValue());

        String resettedPassword = ((MyMockedMailService) mailService).getResettedPassword(userEmail);
        int count = 0;
        while (resettedPassword == null && count < 10) {
            Thread.sleep(1000);
            resettedPassword = ((MyMockedMailService) mailService).getResettedPassword(userEmail);
            count++;
            log.debug("Sleep until we fix better thread waiting.");
        }
        Assertions.assertNotNull(resettedPassword, "Mail has been send");

        log.debug("Reset to " + resettedPassword);

        // inloggen met nieuw wachtwoord
        authRequest = AuthenticationRequest.builder().username(userEmail).password(resettedPassword).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED.value(), responseEntity.getStatusCodeValue());

        // inloggen met oude kan niet meer :
        authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), responseEntity.getStatusCodeValue());

    }

    @Test
    void testChangePassword() {

        String userName = "userChangePassword";
        String userEmail = "userChangePassword@test.example";
        String password = "password1";
        String newPassword = "password2";

        // 1e: keer aanmaken succesvol:
        RegistrationRequest registrationRequest = RegistrationRequest.builder().email(userEmail).password(password).username(userName).build();
        ResponseEntity responseEntity = authenticationService.signUp(registrationRequest);
        Assertions.assertEquals(202, responseEntity.getStatusCodeValue());

        setUserContextFromJWTResponseHeader(responseEntity);

        // 1e keer inloggen met wachtwoord
        AuthenticationRequest authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED.value(), responseEntity.getStatusCodeValue());

        // wachtwoord veranderen
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder().oldPassword(password).newPassword(newPassword).confirmPassword(newPassword).build();
        responseEntity = authenticationService.changePassword(changePasswordRequest);
        Assertions.assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());

        // inloggen met oude kan niet meer:
        authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), responseEntity.getStatusCodeValue());

        // inloggen met nieuwe kan wel:
        authRequest = AuthenticationRequest.builder().username(userEmail).password(newPassword).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED.value(), responseEntity.getStatusCodeValue());
    }

    @Test
    void testDeleteAccount() {

        String userName = "userDeleteAccount";
        String userEmail = "userDeleteAccount@test.example";
        String password = "password1";

        // 1e: keer aanmaken succesvol:
        RegistrationRequest registrationRequest = RegistrationRequest.builder().email(userEmail).password(password).username(userName).build();
        ResponseEntity responseEntity = authenticationService.signUp(registrationRequest);
        Assertions.assertEquals(202, responseEntity.getStatusCodeValue());

        setUserContextFromJWTResponseHeader(responseEntity);

        // 1e keer inloggen met wachtwoord
        AuthenticationRequest authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED.value(), responseEntity.getStatusCodeValue());

        // verwijder met verkeerd wachtwoord afkeuren
        responseEntity = authenticationService.deleteAccount("verkeerd");
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), responseEntity.getStatusCodeValue());

        // verwijder met correct wachtwoord uitvoeren
        deleteAccount(password);

        // inloggen kan niet meer:
        authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getStatusCodeValue());

    }


    @Test
    void deleteFilledAccount() {
        // TODO; Make test to fill a user with meals and portions and stuff and then do a delete
    }

}
