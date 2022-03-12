package slt.servicetests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import slt.dto.*;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.servicetests.utils.AbstractApplicationIntegrationTest;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EntriesServiceITest extends AbstractApplicationIntegrationTest {

    private Integer userId;

    @BeforeAll
    public void setUserContext() {

        if (this.userId == null) {
            log.debug("Creating test user for test " + this.getClass().getName());
            this.userId = createUser(this.getClass().getName());
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(Integer.valueOf(this.userId));
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    public void testCreateLogEntry() {

        String nameFood = "logEntryFood1";

        // 1 create a food without portion:
        FoodDto foodRequestZonderPortions = FoodDto.builder().name(nameFood).carbs(1.0).fat(2.0).protein(3.0).build();
        FoodDto savedFood = createFood(foodRequestZonderPortions);

        String day = "2010-01-01";

        createLogEntry(day, savedFood, null, 1.0);

        // check correctly added:
        List<EntryDto> entriesForDay = getLogEntriesForDay(day);

        assertThat(entriesForDay).hasSize(1);
        assertThat(entriesForDay.get(0).getMeal()).isEqualTo(Meal.BREAKFAST);
        assertThat(entriesForDay.get(0).getMacrosCalculated()).isNotNull();
    }

    @Test
    public void testCreateDubbeleLogEntry() {

        String nameFood = "dubbeleLogEntry";

        // 1 create a food without portion:
        FoodDto foodRequestZonderPortions = FoodDto.builder().name(nameFood).carbs(1.0).fat(2.0).protein(3.0).build();
        FoodDto savedFood = createFood(foodRequestZonderPortions);

        String day = "2001-01-01";

        createLogEntry(day, savedFood, null, 1.0);

        createLogEntry(day, savedFood, null, 3.0);

        // check correctly added:
        List<EntryDto> entriesForDay = getLogEntriesForDay(day);

        assertThat(entriesForDay).hasSize(1);
        assertThat(entriesForDay.get(0).getMeal()).isEqualTo(Meal.BREAKFAST);
        assertThat(entriesForDay.get(0).getMacrosCalculated()).isNotNull();
    }


    @Test
    public void testCreateLogEntryWithPortions() {

        // 1 create a food without portion:
        FoodDto foodRequestMetPortions = FoodDto.builder()
                .name("logEntryFood2")
                .carbs(1.0)
                .fat(2.0)
                .protein(3.0)
                .portions(Arrays.asList(
                        PortionDto.builder().grams(1.0).description("p1").build(),
                        PortionDto.builder().grams(2.0).description("p2").build()
                ))
                .build();
        FoodDto savedFood = createFood(foodRequestMetPortions);
        PortionDto portion1 = savedFood.getPortions().stream().filter(p -> p.getDescription().equals("p1")).findFirst().get();

        // Portion 1 3x
        String day = "2001-01-02";
        createLogEntry(day, savedFood, portion1, 3.0);

        // check correctly added:
        List<EntryDto> entriesForDay = getLogEntriesForDay(day);
        assertThat(entriesForDay).hasSize(1);
        assertThat(entriesForDay.get(0).getMeal()).isEqualTo(Meal.BREAKFAST);
        assertThat(entriesForDay.get(0).getMacrosCalculated()).isNotNull();
        assertThat(entriesForDay.get(0).getFood().getId()).isEqualTo(savedFood.getId());
        assertThat(entriesForDay.get(0).getPortion().getId()).isEqualTo(portion1.getId());
        assertThat(entriesForDay.get(0).getMultiplier()).isEqualTo(3.0);
        assertThat(entriesForDay.get(0).getMacrosCalculated()).isNotNull();
    }

    @Test
    public void testUpdateLogEntry() {
        String nameFood = "updateLogEntryFood";
        String day = "2001-01-03";

        // 1 create a food without portion:
        FoodDto foodRequestZonderPortions = FoodDto.builder().name(nameFood).carbs(1.0).fat(2.0).protein(3.0).build();
        FoodDto savedFood = createFood(foodRequestZonderPortions);

        createLogEntry(day, savedFood, null, 1.0);

        List<EntryDto> entriesForDay = getLogEntriesForDay(day);
        assertThat(entriesForDay).hasSize(1);

        // Create Store Request with update of entry
        List<EntryDto> updateEntries = List.of(
                EntryDto.builder()
                        .day(Date.valueOf(LocalDate.parse(day)))
                        .meal(Meal.valueOf("BREAKFAST"))
                        .portion(null)
                        .food(savedFood)
                        .multiplier(3.0)
                        .id(entriesForDay.get(0).getId())
                        .build()
        );
        ResponseEntity<List<EntryDto>> responseEntity = entriesService.postEntries(day, "BREAKFAST", updateEntries);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value()); // why not CREATED?

        // Check log entry has been updated
        List<EntryDto> updatedForDay = getLogEntriesForDay(day);
        assertThat(updatedForDay).hasSize(1);
        assertThat(updatedForDay.size()).isEqualTo(1);
        assertThat(updatedForDay.get(0).getMultiplier()).isEqualTo(3.0);
    }

    @Test
    public void testDeleteLogEntry() {

        String day = "2001-01-04";
        String nameFood = "foodDeleteLogEntry";

        // 1 create a food without portion:
        FoodDto foodRequestZonderPortions = FoodDto.builder().name(nameFood).carbs(1.0).fat(2.0).protein(3.0).build();
        FoodDto savedFood = createFood(foodRequestZonderPortions);

        createLogEntry(day, savedFood, null, 1.0);

        // check correctly added:
        List<EntryDto> entriesForDay = getLogEntriesForDay(day);
        assertThat(entriesForDay).hasSize(1);

        // delete the entry
        entriesService.deleteLogEntry(entriesForDay.get(0).getId());
        List<EntryDto> afterDeleteForDay = getLogEntriesForDay(day);
        assertThat(afterDeleteForDay).hasSize(0);
    }

    @Test
    public void testGetMacros() {

        String day = "1980-01-05";
        String nameFood = "macrostest";

        // 1 create a food without portion:
        FoodDto foodRequestZonderPortions = FoodDto.builder().name(nameFood).carbs(1.0).fat(2.0).protein(3.0).build();
        FoodDto savedFood = createFood(foodRequestZonderPortions);

        createLogEntry(day, savedFood, null, 1.0);

        // check correctly added:
        List<EntryDto> entriesForDay = getLogEntriesForDay(day);
        assertThat(entriesForDay).hasSize(1);

        ResponseEntity macrosFromPeriod = entriesService.getMacrosFromPeriod("1980-01-05", "1980-01-06");
        assertThat(macrosFromPeriod.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());

        List<DayMacroDto> foundMacros = (List<DayMacroDto>) macrosFromPeriod.getBody();
        assertThat(foundMacros).hasSize(1);
        assertThat(foundMacros.get(0).getMacroDto().getProtein()).isEqualTo(savedFood.getProtein());
        assertThat(foundMacros.get(0).getMacroDto().getFat()).isEqualTo(savedFood.getFat());
        assertThat(foundMacros.get(0).getMacroDto().getCarbs()).isEqualTo(savedFood.getCarbs());

        macrosFromPeriod = entriesService.getMacrosFromPeriod("1980-01-01", "1980-01-05");
        assertThat(macrosFromPeriod.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());

        foundMacros = (List<DayMacroDto>) macrosFromPeriod.getBody();
        assertThat(foundMacros).hasSize(1);

        macrosFromPeriod = entriesService.getMacrosFromPeriod("1980-01-01", "1980-01-04");
        assertThat(macrosFromPeriod.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());

        foundMacros = (List<DayMacroDto>) macrosFromPeriod.getBody();
        assertThat(foundMacros).hasSize(0);


    }

    // TODO test setup moet anders met postEntries
//    @Test
//    public void testGetMacrosMultipleLogEntriesOnADay() {
//
//        String day = "1960-01-05";
//        String nameFood = "macrostestMultipleADay";
//
//        // 1 create a food without portion:
//        FoodDto foodRequestZonderPortions = FoodDto.builder().name(nameFood).carbs(1.0).fat(2.0).protein(3.0).build();
//        FoodDto savedFood = createFood(foodRequestZonderPortions);
//
//        createLogEntry(day, savedFood, null, 3.0);
//
//        // check correctly added:
//        List<EntryDto> entriesForDay = getLogEntriesForDay(day);
//        assertThat(entriesForDay).hasSize(1);
//
//        ResponseEntity macrosFromPeriod = entriesService.getMacrosFromPeriod("1960-01-05", "1960-01-06");
//        assertThat(macrosFromPeriod.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
//
//        List<DayMacroDto> foundMacros = (List<DayMacroDto>) macrosFromPeriod.getBody();
//        assertThat(foundMacros).hasSize(1);
//        assertThat(foundMacros.get(0).getMacro().getProtein()).isEqualTo(savedFood.getProtein() * 4);
//        assertThat(foundMacros.get(0).getMacro().getFat()).isEqualTo(savedFood.getFat() * 4);
//        assertThat(foundMacros.get(0).getMacro().getCarbs()).isEqualTo(savedFood.getCarbs() * 4);
//
//
//        // 4 eenheden en calorieen: 9xfat 4xprotein 4xcarb controle
//        assertThat(foundMacros.get(0).getMacro().getCalories()).
//                isEqualTo(savedFood.getCarbs() * 4 * 4 + savedFood.getProtein() * 4 * 4 + savedFood.getFat() * 4 * 9);
//
//
//        macrosFromPeriod = entriesService.getMacrosFromPeriod("1960-01-01", "1960-01-05");
//        assertThat(macrosFromPeriod.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
//
//        foundMacros = (List<DayMacroDto>) macrosFromPeriod.getBody();
//        assertThat(foundMacros).hasSize(1);
//
//        macrosFromPeriod = entriesService.getMacrosFromPeriod("1960-01-01", "1960-01-04");
//        assertThat(macrosFromPeriod.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
//
//        foundMacros = (List<DayMacroDto>) macrosFromPeriod.getBody();
//        assertThat(foundMacros).hasSize(0);
//
//
//    }


    @Test
    public void testGetMacrosWithPortion() {

        String day = "2002-01-05";
        String nameFood = "macrostestwithPortions";

        // 1 create a food without portion:
        FoodDto foodRequestMetPortions = FoodDto.builder()
                .name(nameFood)
                .carbs(1.0)
                .fat(2.0)
                .protein(3.0)
                .portions(Arrays.asList(
                        PortionDto.builder().grams(1.0).description("p1").build(),
                        PortionDto.builder().grams(2.0).description("p2").build()
                ))
                .build();
        FoodDto savedFood = createFood(foodRequestMetPortions);
        PortionDto portion1 = savedFood.getPortions().stream().filter(p -> p.getDescription().equals("p1")).findFirst().get();

        createLogEntry(day, savedFood, portion1, 1.0);

        // check correctly added:
        List<EntryDto> entriesForDay = getLogEntriesForDay(day);
        assertThat(entriesForDay).hasSize(1);

        ResponseEntity macrosFromPeriod = entriesService.getMacrosFromPeriod("2002-01-01", "2002-01-05");
        assertThat(macrosFromPeriod.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());

        List<DayMacroDto> foundMacros = (List<DayMacroDto>) macrosFromPeriod.getBody();
        assertThat(foundMacros).hasSize(1);
        // potion is 1 gram, dus alles gedeeld door 100
        assertThat(foundMacros.get(0).getMacroDto().getProtein()).isEqualTo(savedFood.getProtein() / 100);
        assertThat(foundMacros.get(0).getMacroDto().getFat()).isEqualTo(savedFood.getFat() / 100);
        assertThat(foundMacros.get(0).getMacroDto().getCarbs()).isEqualTo(savedFood.getCarbs() / 100);


    }

    private List<EntryDto> getLogEntriesForDay(String day) {
        ResponseEntity logEntriesForDay = entriesService.getLogEntriesForDay(day);
        assertThat(logEntriesForDay.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        return (List<EntryDto>) logEntriesForDay.getBody();
    }
}
