package slt.integrationtests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EntryControllerITest extends AbstractApplicationIntegrationTest {

    private Long userId;

    @BeforeAll
    public void setUserContext() {
        if (this.userId == null) {
            log.debug("Creating test user for test {}", this.getClass().getName());
            this.userId = createUser(this.getClass().getName());
        }
        final var userInfo = UserInfo.builder().userId(this.userId).build();
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    public void testCreateLogEntry() {
        final var nameFood = "logEntryFood1";

        // 1 create a food without portion:
        final var foodDtoZonderPortions = FoodDto.builder().name(nameFood).carbs(1.0).fat(2.0).protein(3.0).build();
        final var savedFood = createFood(foodDtoZonderPortions);
        final var day = "2010-01-01";
        createEntry(day, savedFood, null, 1.0);

        // check correctly added:
        final var entriesForDay = getEntriesForDay(day);
        assertThat(entriesForDay).hasSize(1);
        assertThat(entriesForDay.getFirst().getMeal()).isEqualTo(Meal.BREAKFAST);
        assertThat(entriesForDay.getFirst().getMacrosCalculated()).isNotNull();
    }

    @Test
    public void testCreateDubbeleEntry() {
        final var nameFood = "dubbeleLogEntry";

        // 1 create a food without portion:
        final var foodDtoZonderPortions = FoodDto.builder().name(nameFood).carbs(1.0).fat(2.0).protein(3.0).build();
        final var savedFood = createFood(foodDtoZonderPortions);
        final var day = "2001-01-01";
        createEntry(day, savedFood, null, 1.0);
        createEntry(day, savedFood, null, 3.0);

        // check correctly added:
        final var entriesForDay = getEntriesForDay(day);
        assertThat(entriesForDay).hasSize(1);
        assertThat(entriesForDay.getFirst().getMeal()).isEqualTo(Meal.BREAKFAST);
        assertThat(entriesForDay.getFirst().getMacrosCalculated()).isNotNull();
    }

    @Test
    public void testCreateLogEntryWithPortions() {
        // 1 create a food without portion:
        final var foodDtoMetPortions = FoodDto.builder()
                .name("logEntryFood2")
                .carbs(1.0)
                .fat(2.0)
                .protein(3.0)
                .portions(Arrays.asList(
                        PortionDto.builder().grams(1.0).description("p1").build(),
                        PortionDto.builder().grams(2.0).description("p2").build()
                ))
                .build();
        final var savedFood = createFood(foodDtoMetPortions);
        final var optionalPortion1 = savedFood.getPortions().stream().filter(p -> p.getDescription().equals("p1")).findFirst();
        Assertions.assertTrue(optionalPortion1.isPresent());
        final var portion1 = optionalPortion1.get();

        // Portion 1 3x
        final var day = "2001-01-02";
        createEntry(day, savedFood, portion1, 3.0);

        // check correctly added:
        final var entriesForDay = getEntriesForDay(day);
        assertThat(entriesForDay).hasSize(1);
        assertThat(entriesForDay.getFirst().getMeal()).isEqualTo(Meal.BREAKFAST);
        assertThat(entriesForDay.getFirst().getMacrosCalculated()).isNotNull();
        assertThat(entriesForDay.getFirst().getFood().getId()).isEqualTo(savedFood.getId());
        assertThat(entriesForDay.getFirst().getPortion().getId()).isEqualTo(portion1.getId());
        assertThat(entriesForDay.getFirst().getMultiplier()).isEqualTo(3.0);
        assertThat(entriesForDay.getFirst().getMacrosCalculated()).isNotNull();
    }

    @Test
    public void testUpdateLogEntry() {
        final var nameFood = "updateLogEntryFood";
        final var day = "2001-01-03";

        // 1 create a food without portion:
        final var foodDtoZonderPortions = FoodDto.builder().name(nameFood).carbs(1.0).fat(2.0).protein(3.0).build();
        final var savedFood = createFood(foodDtoZonderPortions);
        createEntry(day, savedFood, null, 1.0);
        final var entriesForDay = getEntriesForDay(day);
        assertThat(entriesForDay).hasSize(1);

        // Create Store Request with update of entry
        final var updateEntries = List.of(
                EntryDto.builder()
                        .day(Date.valueOf(LocalDate.parse(day)))
                        .meal(Meal.valueOf("BREAKFAST"))
                        .portion(null)
                        .food(savedFood)
                        .multiplier(3.0)
                        .id(entriesForDay.getFirst().getId())
                        .build()
        );
        final var responseEntity = entryController.postEntries(day, "BREAKFAST", updateEntries);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK); // why not CREATED?

        // Check log entry has been updated
        final var updatedForDay = getEntriesForDay(day);
        assertThat(updatedForDay).hasSize(1);
        assertThat(updatedForDay.size()).isEqualTo(1);
        assertThat(updatedForDay.getFirst().getMultiplier()).isEqualTo(3.0);
    }

    @Test
    public void testDeleteEntry() {
        final var day = "2001-01-04";
        final var nameFood = "foodDeleteLogEntry";

        // 1 create a food without portion:
        final var foodDtoZonderPortions = FoodDto.builder().name(nameFood).carbs(1.0).fat(2.0).protein(3.0).build();
        final var savedFood = createFood(foodDtoZonderPortions);
        createEntry(day, savedFood, null, 1.0);
        final var entriesForDay = getEntriesForDay(day);
        assertThat(entriesForDay).hasSize(1);

        // delete the entry
        entryController.deleteEntry(entriesForDay.getFirst().getId());
        final var afterDeleteForDay = getEntriesForDay(day);
        assertThat(afterDeleteForDay).hasSize(0);
    }

    @Test
    public void testGetMacros() {
        final var day = "1980-01-05";
        final var nameFood = "macrostest";

        // 1 create a food without portion:
        final var foodDtoZonderPortions = FoodDto.builder().name(nameFood).carbs(1.0).fat(2.0).protein(3.0).build();
        final var savedFood = createFood(foodDtoZonderPortions);
        createEntry(day, savedFood, null, 1.0);
        final var entriesForDay = getEntriesForDay(day);
        assertThat(entriesForDay).hasSize(1);

        var macrosFromPeriod = entryController.getMacrosForPeriod("1980-01-05", "1980-01-06");
        assertThat(macrosFromPeriod.getStatusCode()).isEqualTo(HttpStatus.OK);

        var foundMacros = macrosFromPeriod.getBody();
        assertThat(foundMacros).hasSize(1);
        assertThat(foundMacros.getFirst().getMacros().getProtein()).isEqualTo(savedFood.getProtein());
        assertThat(foundMacros.getFirst().getMacros().getFat()).isEqualTo(savedFood.getFat());
        assertThat(foundMacros.getFirst().getMacros().getCarbs()).isEqualTo(savedFood.getCarbs());

        macrosFromPeriod = entryController.getMacrosForPeriod("1980-01-01", "1980-01-05");
        assertThat(macrosFromPeriod.getStatusCode()).isEqualTo(HttpStatus.OK);

        foundMacros = macrosFromPeriod.getBody();
        assertThat(foundMacros).hasSize(1);

        macrosFromPeriod = entryController.getMacrosForPeriod("1980-01-01", "1980-01-04");
        assertThat(macrosFromPeriod.getStatusCode()).isEqualTo(HttpStatus.OK);

        foundMacros = macrosFromPeriod.getBody();
        assertThat(foundMacros).hasSize(0);
    }

    @Test
    public void testGetMacrosWithPortion() {
        final var day = "2002-01-05";
        final var nameFood = "macrostestwithPortions";

        // 1 create a food without portion:
        final var foodDtoZonderPortions = FoodDto.builder()
                .name(nameFood)
                .carbs(1.0)
                .fat(2.0)
                .protein(3.0)
                .portions(Arrays.asList(
                        PortionDto.builder().grams(1.0).description("p1").build(),
                        PortionDto.builder().grams(2.0).description("p2").build()
                ))
                .build();
        final var savedFood = createFood(foodDtoZonderPortions);
        final var optionalPortion1 = savedFood.getPortions().stream().filter(p -> p.getDescription().equals("p1")).findFirst();
        Assertions.assertTrue(optionalPortion1.isPresent());
        final var portion1 = optionalPortion1.get();

        createEntry(day, savedFood, portion1, 1.0);

        // check correctly added:
        final var entriesForDay = getEntriesForDay(day);
        assertThat(entriesForDay).hasSize(1);

        final var macrosFromPeriod = entryController.getMacrosForPeriod("2002-01-01", "2002-01-05");
        assertThat(macrosFromPeriod.getStatusCode()).isEqualTo(HttpStatus.OK);

        final var foundMacros = macrosFromPeriod.getBody();
        assertThat(foundMacros).hasSize(1);
        assertThat(foundMacros.getFirst().getMacros().getProtein()).isEqualTo(savedFood.getProtein() / 100);
        assertThat(foundMacros.getFirst().getMacros().getFat()).isEqualTo(savedFood.getFat() / 100);
        assertThat(foundMacros.getFirst().getMacros().getCarbs()).isEqualTo(savedFood.getCarbs() / 100);
    }

    private List<EntryDto> getEntriesForDay(String day) {
        final var entriesForDay = entryController.getEntriesForDay(day);
        assertThat(entriesForDay.getStatusCode()).isEqualTo(HttpStatus.OK);
        return entriesForDay.getBody();
    }
}
