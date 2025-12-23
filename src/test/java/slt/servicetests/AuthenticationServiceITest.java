package slt.servicetests;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import slt.dto.*;
import slt.servicetests.utils.AbstractApplicationIntegrationTest;
import slt.servicetests.utils.MyMockedMailService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class AuthenticationServiceITest extends AbstractApplicationIntegrationTest {

    @Test
    void testSignupNewUser() {
        String userName = "newuser";
        String userEmail = "newuser@test.example";

        RegistrationRequest registrationRequest = RegistrationRequest.builder().email(userEmail).password("testpassword").username(userName).build();
        ResponseEntity<UserAccountDto> responseEntity = authenticationService.signUp(registrationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());
        String jwtToken = Objects.requireNonNull(responseEntity.getHeaders().get("token")).getFirst();
        log.debug(jwtToken);
        Jws<Claims> claimsJws = getClaimsJws(jwtToken);
        Integer userId = (Integer) claimsJws.getBody().get("userId");
        log.debug("User id = " + userId);

    }

    @Test
    void testSignupUserOrEmailAlreadyKnown() {

        String userName = "userknown";
        String userEmail = "emailknown@test.example";

        // 1e: keer aanmaken succesvol:
        RegistrationRequest registrationRequest = RegistrationRequest.builder().email(userEmail).password("testpassword").username(userName).build();
        var responseEntity = authenticationService.signUp(registrationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        boolean mailSend = ((MyMockedMailService) mailService).verifyConfirmationSendTo(userEmail);
        Assertions.assertTrue(mailSend, "Mail has been send");

        // 2e: keer afgekeurd op username
        registrationRequest = RegistrationRequest.builder().email("diffemail@email.com").password("testpassword").username(userName).build();
        responseEntity = authenticationService.signUp(registrationRequest);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        mailSend = ((MyMockedMailService) mailService).verifyConfirmationSendTo(userEmail);
        Assertions.assertFalse(mailSend, "Mail should not be send");

        // 3e: keer afgekeurd op email
        registrationRequest = RegistrationRequest.builder().email(userEmail).password("testpassword").username("diffusername").build();
        responseEntity = authenticationService.signUp(registrationRequest);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        mailSend = ((MyMockedMailService) mailService).verifyConfirmationSendTo(userEmail);
        Assertions.assertFalse(mailSend, "Mail should not be send");

    }

    @Test
    void testResetPassword() {

        String userName = "userResetPassword";
        String userEmail = "userResetPassword@test.example";
        String password = "password1";

        // 1e: keer aanmaken succesvol:
        RegistrationRequest registrationRequest = RegistrationRequest.builder().email(userEmail).password(password).username(userName).build();
        var responseEntity = authenticationService.signUp(registrationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        setUserContextFromJWTResponseHeader(responseEntity);

        // 1e keer inloggen met wachtwoord
        AuthenticationRequest authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        // wachtwoord resetten
        ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder().email(userEmail).build();
        final var responseEntity1 = authenticationService.resetPassword(resetPasswordRequest);
        Assertions.assertEquals(HttpStatus.OK, responseEntity1.getStatusCode());

        // inloggen met oude kan nog:
        authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        String resettedPassword = ((MyMockedMailService) mailService).getResettedPassword(userEmail);
        Assertions.assertNotNull(resettedPassword, "Mail has been send");

        log.debug("Reset to " + resettedPassword);

        // inloggen met nieuw wachtwoord
        authRequest = AuthenticationRequest.builder().username(userEmail).password(resettedPassword).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        // inloggen met oude kan niet meer :
        authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

    }

    @Test
    void testChangePassword() {

        String userName = "userChangePassword";
        String userEmail = "userChangePassword@test.example";
        String password = "password1";
        String newPassword = "password2";

        // 1e: keer aanmaken succesvol:
        RegistrationRequest registrationRequest = RegistrationRequest.builder().email(userEmail).password(password).username(userName).build();
        ResponseEntity<UserAccountDto> responseEntity = authenticationService.signUp(registrationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        setUserContextFromJWTResponseHeader(responseEntity);

        // 1e keer inloggen met wachtwoord
        AuthenticationRequest authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        // wachtwoord veranderen
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder().oldPassword(password).newPassword(newPassword).confirmPassword(newPassword).build();
        ResponseEntity<Void> result = authenticationService.changePassword(changePasswordRequest);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());

        // inloggen met oude kan niet meer:
        authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

        // inloggen met nieuwe kan wel:
        authRequest = AuthenticationRequest.builder().username(userEmail).password(newPassword).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());
    }

    @Test
    void testDeleteAccount() {

        String userName = "userDeleteAccount";
        String userEmail = "userDeleteAccount@test.example";
        String password = "password1";

        // 1e: keer aanmaken succesvol:
        RegistrationRequest registrationRequest = RegistrationRequest.builder().email(userEmail).password(password).username(userName).build();
        ResponseEntity<UserAccountDto> responseEntity = authenticationService.signUp(registrationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        setUserContextFromJWTResponseHeader(responseEntity);

        // 1e keer inloggen met wachtwoord
        AuthenticationRequest authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        // verwijder met verkeerd wachtwoord afkeuren
        ResponseEntity<Void> result = authenticationService.deleteAccount("verkeerd");
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());

        // verwijder met correct wachtwoord uitvoeren
        deleteAccount(password);

        // inloggen kan niet meer:
        authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());

    }

    @Test
    void testDeleteAccountTwice() {

        String userName = "userDeleteAccount";
        String userEmail = "userDeleteAccount@test.example";
        String password = "password1";

        // 1e: keer aanmaken succesvol:
        RegistrationRequest registrationRequest = RegistrationRequest.builder().email(userEmail).password(password).username(userName).build();
        ResponseEntity<UserAccountDto> responseEntity = authenticationService.signUp(registrationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        setUserContextFromJWTResponseHeader(responseEntity);

        // 1e keer inloggen met wachtwoord
        AuthenticationRequest authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationService.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());


        // verwijder met correct wachtwoord uitvoeren
        deleteAccount(password);

        // verwijder nomaals
        ResponseEntity<Void> result = authenticationService.deleteAccount(password);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());

    }


    @Test
    void deleteFilledAccount() {
        String userName = "filledUserToDelete";
        String userEmail = "filledUserToDelete@test.example";
        String password = "password1";

        // 1e: keer aanmaken succesvol:
        RegistrationRequest registrationRequest = RegistrationRequest.builder().email(userEmail).password(password).username(userName).build();
        ResponseEntity<UserAccountDto> responseEntity = authenticationService.signUp(registrationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        setUserContextFromJWTResponseHeader(responseEntity);

        // add food zonder portion
        FoodDto foodRequestZonderPortions = FoodDto.builder().name("exportFoodNoPortion").carbs(1.0).fat(2.0).protein(3.0).build();
        FoodDto foodZonderPortion = createFood(foodRequestZonderPortions);

        // add food met portion
        FoodDto foodRequestMetPortions = FoodDto.builder()
                .name("exportFoodWithPortion")
                .carbs(1.0)
                .fat(2.0)
                .protein(3.0)
                .portions(Arrays.asList(
                                PortionDto.builder()
                                        .description("portion1")
                                        .grams(200.0)
                                        .build(),
                                PortionDto.builder()
                                        .description("portion2")
                                        .grams(300.0)
                                        .build()
                        )
                )
                .build();
        FoodDto savedFood = createFood(foodRequestMetPortions);
        PortionDto portion1 = savedFood.getPortions().stream().filter(p ->
                p.getDescription().equals("portion1")).findFirst().get();

        // add log entry without portion
        String day = "2001-01-02";
        createLogEntry(day, foodZonderPortion, null, 3.0);

        // add log entry with portion
        createLogEntry(day, savedFood, portion1, 3.0);

        // add activity
        List<LogActivityDto> newActivities = Arrays.asList(
                LogActivityDto.builder()
                        .day(Date.valueOf(LocalDate.parse("2003-01-01")))
                        .name("Running")
                        .calories(20.0)
                        .build(),
                LogActivityDto.builder()
                        .day(Date.valueOf(LocalDate.parse("2003-01-01")))
                        .name("Cycling")
                        .calories(30.0)
                        .build()

        );
        ResponseEntity<List<LogActivityDto>> result = activityService.postActivities("2003-01-01", newActivities);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        // add weight
        // store weight:
        WeightDto newWeight = WeightDto.builder()
                .weight(10.0)
                .day(LocalDate.parse("1980-01-01"))
                .build();
        ResponseEntity<WeightDto> result2 = weightController.postWeight(newWeight);
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.OK);

        // add settings:
        saveSetting("export1", "export1value");

        deleteAccount(password);

    }

}
