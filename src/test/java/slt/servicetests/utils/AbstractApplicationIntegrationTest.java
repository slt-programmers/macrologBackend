package slt.servicetests.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


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
    protected EntriesService entriesService;

    @Autowired
    protected FoodController foodController;

    @Autowired
    protected AuthenticationService authenticationService;

    @Autowired
    protected SettingsController settingsController;

    @Autowired
    protected WeightController weightController;

    @Autowired
    protected ImportController importController;

    @Autowired
    protected ExportService exportService;

    @Autowired
    protected DishService dishService;

    @Autowired
    protected AdminService adminService;

    @Autowired
    protected AccountService accountService;

    @Autowired
    protected UserAccountRepository userAccountRepository;

    @Autowired
    protected PortionRepository portionRepository;

    @Autowired
    protected IngredientRepository ingredientRepository;

    protected Long createUser(final String userEmail) {
        RegistrationRequest registrationRequest = RegistrationRequest.builder().email(userEmail).password("testpassword").username(userEmail).build();
        ResponseEntity<UserAccountDto> responseEntity = authenticationService.signUp(registrationRequest);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());
        return getUserIdFromResponseHeaderJWT(responseEntity);
    }

    protected Long getUserIdFromResponseHeaderJWT(ResponseEntity<UserAccountDto> responseEntity) {
        final var jwtToken = Objects.requireNonNull(responseEntity.getHeaders().get("token")).getFirst();
        final var claims = getClaimsJws(jwtToken);
        final var userId = claims.getBody().get("userId");
        log.debug("User id = " + userId);
        Assert.notNull(userId, "Geen UserID te herleiden");
        return Long.valueOf(userId.toString());
    }

    protected void setUserContextFromJWTResponseHeader(ResponseEntity<UserAccountDto> responseEntity) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(getUserIdFromResponseHeaderJWT(responseEntity));
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    protected void deleteAccount(String password) {
        ResponseEntity<Void> responseEntity = authenticationService.deleteAccount(password);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    protected Jws<Claims> getClaimsJws(String jwtToken) {
        return Jwts.parser()
                .setSigningKey(SecurityConstants.SECRET.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(jwtToken);
    }

    protected boolean isEqualDate(Date date, LocalDate localDate) {
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault()).toLocalDate().equals(localDate);
    }

    protected FoodDto createFood(FoodDto foodRequestZonderPortions) {
        ResponseEntity<FoodDto> responseEntity = foodController.postFood(foodRequestZonderPortions);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        ResponseEntity<List<FoodDto>> allFoodEntity = foodController.getAllFood();
        assertThat(allFoodEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<FoodDto> foodDtos = allFoodEntity.getBody();
        Assertions.assertNotNull(foodDtos);
        return foodDtos.stream().filter(f -> f.getName().equals(foodRequestZonderPortions.getName())).findFirst().get();
    }

    protected void createLogEntry(String day, FoodDto savedFood, PortionDto portion, double multiplier) {
        List<EntryDto> newLogEntries = List.of(
                EntryDto.builder()
                        .day(java.sql.Date.valueOf(LocalDate.parse(day)))
                        .meal(Meal.valueOf("BREAKFAST"))
                        .portion(portion)
                        .food(savedFood)
                        .multiplier(multiplier)
                        .build()
        );
        ResponseEntity<List<EntryDto>> responseEntity = entriesService.postEntries(day, "BREAKFAST", newLogEntries);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK); // why not CREATED?
        Assertions.assertNotNull(responseEntity.getBody());
        getMatch(responseEntity.getBody(), savedFood, portion, multiplier);
    }

    private void getMatch(List<EntryDto> all, FoodDto foodDto, PortionDto portionDto, double multiplier) {
        List<EntryDto> matches = all.stream()
                .filter(entryDto -> entryDto.getMultiplier().equals(multiplier) &&
                        (portionDto == null || entryDto.getPortion().getId().equals(portionDto.getId())) &&
                        entryDto.getFood().getId().equals(foodDto.getId())
                )
                .collect(Collectors.toList());
        assertThat(matches).hasSize(1);
        matches.getFirst();
    }

    protected void saveSetting(final String name, final String value) {
        SettingDto settingDto = SettingDto.builder().name(name).value(value).build();
        ResponseEntity<Void> responseEntity = settingsController.putSetting(settingDto);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    protected void saveSetting(final String name, final String value, final LocalDate day) {
        final var settingDto = SettingDto.builder().name(name).value(value).day(day).build();
        final var responseEntity = settingsController.putSetting(settingDto);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
