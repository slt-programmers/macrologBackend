package slt.integrationtests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;
import slt.dto.*;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.integrationtests.utils.AbstractApplicationIntegrationTest;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ImportExportControllerITest extends AbstractApplicationIntegrationTest {

    private Long userId;

    @BeforeEach
    public void setUserContext() {
        if (this.userId == null) {
            log.debug("Creating test user for test {}", this.getClass());
            this.userId = createUser("logEntryUser");
        }
        final var userInfo = UserInfo.builder().userId(this.userId).build();
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    public void testItAll() {
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
        final var optionalPortion1 = savedFood.getPortions().stream().filter(p -> p.getDescription().equals("portion1")).findFirst();
        Assertions.assertTrue(optionalPortion1.isPresent());
        final var portion1 = optionalPortion1.get();

        // add log entry without portion
        final var day = "2001-01-02";
        createEntry(day, foodZonderPortion, null, 3.0);

        // add log entry with portion
        createEntry(day, savedFood, portion1, 3.0);

        // add activity
        final var newActivities = Arrays.asList(
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
        final var responseEntity = activityController.postActivities("2003-01-01", newActivities);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // add weight
        // store weight:
        final var newWeight = WeightDto.builder()
                .weight(10.0)
                .day(LocalDate.parse("1980-01-01"))
                .build();
        final var weightResponse = weightController.postWeight(newWeight);
        Assertions.assertEquals(HttpStatus.OK, weightResponse.getStatusCode());

        // add settings:
        saveSetting("export1", "export1value");

        final var exportEntity = exportController.getAll();
        Assertions.assertEquals(HttpStatus.OK, exportEntity.getStatusCode());
        final var export = exportEntity.getBody();

        Assertions.assertNotNull(export);
        Assertions.assertEquals(2, export.getAllFood().size());
        final var optionalFood = export.getAllFood().stream().filter(f -> f.getName().equals("exportFoodWithPortion")).findFirst();
        Assertions.assertTrue(optionalFood.isPresent());
        Assertions.assertEquals(2, optionalFood.get().getPortions().size());
        final var optionalFood2 = export.getAllFood().stream().filter(f -> f.getName().equals("exportFoodNoPortion")).findFirst();
        Assertions.assertTrue(optionalFood2.isPresent());
        Assertions.assertEquals(0, optionalFood2.get().getPortions().size());

        Assertions.assertEquals(1, export.getAllLogEntries().size());
        Assertions.assertEquals(2, export.getAllActivities().size());
        Assertions.assertEquals(1, export.getAllWeights().size());
        Assertions.assertEquals(1, export.getAllSettingDtos().size());

        final var importEntity = importController.setAll(export);
        Assertions.assertEquals(HttpStatus.OK, importEntity.getStatusCode());
    }

}
