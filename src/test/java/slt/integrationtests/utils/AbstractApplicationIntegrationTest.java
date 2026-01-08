package slt.integrationtests.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;
import slt.Application;
import slt.database.IngredientRepository;
import slt.database.PortionRepository;
import slt.database.UserAccountRepository;
import slt.dto.*;
import slt.rest.*;
import slt.security.SecurityConstants;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.service.AccountService;
import slt.service.GoogleMailService;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
    protected GoogleMailService mailService;

    @Autowired
    protected ActivityController activityController;

    @Autowired
    protected EntryController entryController;

    @Autowired
    protected FoodController foodController;

    @Autowired
    protected AuthenticationController authenticationController;

    @Autowired
    protected SettingsController settingsController;

    @Autowired
    protected WeightController weightController;

    @Autowired
    protected ImportController importController;

    @Autowired
    protected ExportController exportController;

    @Autowired
    protected DishController dishController;

    @Autowired
    protected AdminController adminController;

    @Autowired
    protected AccountService accountService;

    @Autowired
    protected UserAccountRepository userAccountRepository;

    @Autowired
    protected PortionRepository portionRepository;

    @Autowired
    protected IngredientRepository ingredientRepository;

    protected Long createUser(final String userEmail) {
        final var registrationRequest = RegistrationRequest.builder().email(userEmail).password("testpassword").username(userEmail).build();
        final var responseEntity = authenticationController.signUp(registrationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());
        final var headers = responseEntity.getHeaders();
        return getUserIdFromResponseHeaderJWT(headers);
    }

    protected Long getUserIdFromResponseHeaderJWT(final HttpHeaders headers) {
        final var jwtToken = Objects.requireNonNull(headers.get("token")).getFirst();
        final var claims = getClaimsJws(jwtToken);
        final var userId = claims.getBody().get("userId");
        log.debug("UserId  [{}]", userId);
        Assert.notNull(userId, "Geen UserId te herleiden");
        return Long.valueOf(userId.toString());
    }

    protected void setUserContextFromJWTResponseHeader(final HttpHeaders headers) {
        final var userInfo = UserInfo.builder().userId(getUserIdFromResponseHeaderJWT(headers)).build();
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    protected void deleteAccount(final String password) {
        final var responseEntity = authenticationController.deleteAccount(password);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    protected Jws<Claims> getClaimsJws(final String jwtToken) {
        return Jwts.parser()
                .setSigningKey(SecurityConstants.SECRET.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(jwtToken);
    }

    protected boolean isEqualDate(final Date date, final LocalDate localDate) {
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault()).toLocalDate().equals(localDate);
    }

    protected FoodDto createFood(final FoodDto foodDtoZonderPortions) {
        final var responseEntity = foodController.postFood(foodDtoZonderPortions);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        final var foodDto = responseEntity.getBody();
        Assertions.assertNotNull(foodDto);
        return foodDto;
    }

    protected void createEntry(final String day, final FoodDto savedFood, final PortionDto portion, double multiplier) {
        final var newEntries = List.of(
                EntryDto.builder()
                        .day(java.sql.Date.valueOf(LocalDate.parse(day)))
                        .meal(Meal.valueOf("BREAKFAST"))
                        .portion(portion)
                        .food(savedFood)
                        .multiplier(multiplier)
                        .build()
        );
        final var responseEntity = entryController.postEntries(day, "BREAKFAST", newEntries);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody());
        checkForSingleMatch(responseEntity.getBody(), savedFood, portion, multiplier);
    }

    private void checkForSingleMatch(final List<EntryDto> all, final FoodDto foodDto, final PortionDto portionDto, double multiplier) {
        final var matches = all.stream()
                .filter(entryDto -> entryDto.getMultiplier().equals(multiplier) &&
                        (portionDto == null || entryDto.getPortion().getId().equals(portionDto.getId())) &&
                        entryDto.getFood().getId().equals(foodDto.getId())
                )
                .toList();
        Assertions.assertEquals(1, matches.size());
    }

    protected void saveSetting(final String name, final String value) {
        final var settingDto = SettingDto.builder().name(name).value(value).build();
        final var responseEntity = settingsController.putSetting(settingDto);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    protected void saveNameSetting(final String value, final LocalDate day) {
        final var settingDto = SettingDto.builder().name("name").value(value).day(day).build();
        final var responseEntity = settingsController.putSetting(settingDto);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
