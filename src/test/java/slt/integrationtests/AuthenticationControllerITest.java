package slt.integrationtests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import slt.dto.*;
import slt.exceptions.NotFoundException;
import slt.exceptions.UnauthorizedException;
import slt.integrationtests.utils.AbstractApplicationIntegrationTest;
import slt.integrationtests.utils.MyMockedMailService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
class AuthenticationControllerITest extends AbstractApplicationIntegrationTest {

    @Test
    void testSignupNewUser() {
        final var userName = "newuser";
        final var userEmail = "newuser@test.example";

        final var registrationRequest = RegistrationRequest.builder().email(userEmail).password("testpassword").username(userName).build();
        final var responseEntity = authenticationController.signUp(registrationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        final var jwtToken = Objects.requireNonNull(responseEntity.getHeaders().get("token")).getFirst();
        final var claimsJws = getClaimsJws(jwtToken);
        final var userId = claimsJws.getBody().get("userId");
        log.debug("UserId [{}]", userId);
    }

    @Test
    void testSignupUserOrEmailAlreadyKnown() {
        final var userName = "userknown";
        final var userEmail = "emailknown@test.example";

        // 1e: keer aanmaken succesvol:
        final var registrationRequest = RegistrationRequest.builder().email(userEmail).password("testpassword").username(userName).build();
        var responseEntity = authenticationController.signUp(registrationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        boolean mailSend = ((MyMockedMailService) mailService).verifyConfirmationSendTo(userEmail);
        Assertions.assertTrue(mailSend, "Mail has been send");

        // 2e: keer afgekeurd op username
        final var registrationRequest2 = RegistrationRequest.builder().email("diffemail@email.com").password("testpassword").username(userName).build();
        Assertions.assertThrows(UnauthorizedException.class, () -> authenticationController.signUp(registrationRequest2));
        mailSend = ((MyMockedMailService) mailService).verifyConfirmationSendTo(userEmail);
        Assertions.assertFalse(mailSend, "Mail should not be send");

        // 3e: keer afgekeurd op email
        final var registrationRequest3 = RegistrationRequest.builder().email(userEmail).password("testpassword").username("diffusername").build();
        Assertions.assertThrows(UnauthorizedException.class, () -> authenticationController.signUp(registrationRequest3));
        mailSend = ((MyMockedMailService) mailService).verifyConfirmationSendTo(userEmail);
        Assertions.assertFalse(mailSend, "Mail should not be send");
    }

    @Test
    void testResetPassword() {
        final var userName = "userResetPassword";
        final var userEmail = "userResetPassword@test.example";
        final var password = "password1";

        // 1e: keer aanmaken succesvol:
        final var registrationRequest = RegistrationRequest.builder().email(userEmail).password(password).username(userName).build();
        var responseEntity = authenticationController.signUp(registrationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        setUserContextFromJWTResponseHeader(responseEntity.getHeaders());

        // 1e keer inloggen met wachtwoord
        final var authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationController.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        // wachtwoord resetten
        final var resetPasswordRequest = ResetPasswordRequest.builder().email(userEmail).build();
        final var responseEntity1 = authenticationController.resetPassword(resetPasswordRequest);
        Assertions.assertEquals(HttpStatus.OK, responseEntity1.getStatusCode());

        // inloggen met oude kan nog:
        final var authRequest2 = AuthenticationRequest.builder().username(userEmail).password(password).build();
        responseEntity = authenticationController.authenticateUser(authRequest2);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        final var resettedPassword = ((MyMockedMailService) mailService).getResettedPassword(userEmail);
        Assertions.assertNotNull(resettedPassword, "Mail has been send");

        log.debug("Reset to {}", resettedPassword);

        // inloggen met nieuw wachtwoord
        final var authRequest3 = AuthenticationRequest.builder().username(userEmail).password(resettedPassword).build();
        responseEntity = authenticationController.authenticateUser(authRequest3);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        // inloggen met oude kan niet meer :
        final var authRequest4 = AuthenticationRequest.builder().username(userEmail).password(password).build();
        Assertions.assertThrows(UnauthorizedException.class, () -> authenticationController.authenticateUser(authRequest4));
    }

    @Test
    void testChangePassword() {
        final var userName = "userChangePassword";
        final var userEmail = "userChangePassword@test.example";
        final var password = "password1";
        final var newPassword = "password2";

        // 1e: keer aanmaken succesvol:
        final var registrationRequest = RegistrationRequest.builder().email(userEmail).password(password).username(userName).build();
        final  var responseEntity = authenticationController.signUp(registrationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        setUserContextFromJWTResponseHeader(responseEntity.getHeaders());

        // 1e keer inloggen met wachtwoord
        final var authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        final  var responseEntity2 = authenticationController.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity2.getStatusCode());

        // wachtwoord veranderen
        final var changePasswordRequest = ChangePasswordRequest.builder().oldPassword(password).newPassword(newPassword).confirmPassword(newPassword).build();
       final var result = authenticationController.changePassword(changePasswordRequest);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());

        // inloggen met oude kan niet meer:
        final  var authRequest2 = AuthenticationRequest.builder().username(userEmail).password(password).build();
        Assertions.assertThrows(UnauthorizedException.class, () -> authenticationController.authenticateUser(authRequest2));

        // inloggen met nieuwe kan wel:
        final  var authRequest3 = AuthenticationRequest.builder().username(userEmail).password(newPassword).build();
        final var responseEntity1 = authenticationController.authenticateUser(authRequest3);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity1.getStatusCode());
    }

    @Test
    void testDeleteAccount() {
        final var userName = "userDeleteAccount";
        final var userEmail = "userDeleteAccount@test.example";
        final var password = "password2";

        // 1e: keer aanmaken succesvol:
        final var registrationRequest = RegistrationRequest.builder().email(userEmail).password(password).username(userName).build();
        final var responseEntity = authenticationController.signUp(registrationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        setUserContextFromJWTResponseHeader(responseEntity.getHeaders());

        // 1e keer inloggen met wachtwoord
        final var authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        final  var responseEntity1 = authenticationController.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity1.getStatusCode());

        // verwijder met verkeerd wachtwoord afkeuren
        Assertions.assertThrows(UnauthorizedException.class, () -> authenticationController.deleteAccount("verkeerd"));

        // verwijder met correct wachtwoord uitvoeren
        deleteAccount(password);

        // inloggen kan niet meer:
        final  var authRequest1 = AuthenticationRequest.builder().username(userEmail).password(password).build();
        Assertions.assertThrows(NotFoundException.class, () -> authenticationController.authenticateUser(authRequest1));
    }

    @Test
    void testDeleteAccountTwice() {
        final var userName = "userDeleteAccount";
        final var userEmail = "userDeleteAccount@test.example";
        final var password = "password2";

        // 1e: keer aanmaken succesvol:
        final var registrationRequest = RegistrationRequest.builder().email(userEmail).password(password).username(userName).build();
        final var responseEntity = authenticationController.signUp(registrationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        setUserContextFromJWTResponseHeader(responseEntity.getHeaders());

        // 1e keer inloggen met wachtwoord
        final var authRequest = AuthenticationRequest.builder().username(userEmail).password(password).build();
        final  var responseEntity1 = authenticationController.authenticateUser(authRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity1.getStatusCode());

        // verwijder met correct wachtwoord uitvoeren
        deleteAccount(password);

        // verwijder nomaals
        Assertions.assertThrows(NotFoundException.class, () -> authenticationController.deleteAccount(password));
    }

    @Test
    void deleteFilledAccount() {
        final var userName = "filledUserToDelete";
        final var userEmail = "filledUserToDelete@test.example";
        final var password = "password3";

        // 1e: keer aanmaken succesvol:
        final var registrationRequest = RegistrationRequest.builder().email(userEmail).password(password).username(userName).build();
        final var responseEntity = authenticationController.signUp(registrationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        setUserContextFromJWTResponseHeader(responseEntity.getHeaders());

        // add food zonder portion
        final var foodDtoZonderPortions = FoodDto.builder().name("exportFoodNoPortion").carbs(1.0).fat(2.0).protein(3.0).build();
        final var foodZonderPortion = createFood(foodDtoZonderPortions);

        // add food met portion
        final var foodDtoMetPortions = FoodDto.builder()
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
        final var savedFood = createFood(foodDtoMetPortions);
        final var optionalPortion1 = savedFood.getPortions().stream().filter(p ->
                p.getDescription().equals("portion1")).findFirst();
        Assertions.assertTrue(optionalPortion1.isPresent());
        final var portion1 = optionalPortion1.get();

        // add log entry without portion
        final var day = "2001-01-02";
        createEntry(day, foodZonderPortion, null, 3.0);

        // add log entry with portion
        createEntry(day, savedFood, portion1, 3.0);

        // add activity
        final  var newActivities = Arrays.asList(
                ActivityDto.builder()
                        .day(Date.valueOf(LocalDate.parse("2003-01-01")))
                        .name("Running")
                        .calories(20.0)
                        .build(),
                ActivityDto.builder()
                        .day(Date.valueOf(LocalDate.parse("2003-01-01")))
                        .name("Cycling")
                        .calories(30.0)
                        .build()

        );
        final var result = activityController.postActivities("2003-01-01", newActivities);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());

        // add weight
        // store weight:
        final var newWeight = WeightDto.builder()
                .weight(10.0)
                .day(LocalDate.parse("1980-01-01"))
                .build();
        final var result2 = weightController.postWeight(newWeight);
        Assertions.assertEquals(HttpStatus.OK, result2.getStatusCode());

        // add settings:
        saveSetting("export1", "export1value");
        deleteAccount(password);
    }

}
