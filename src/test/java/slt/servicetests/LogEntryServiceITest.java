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
public class LogEntryServiceITest extends AbstractApplicationIntegrationTest {

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
        AddFoodRequest addFoodRequestZonderPortions = AddFoodRequest.builder().name(nameFood).carbs(1.0).fat(2.0).protein(3.0).build();
        FoodDto savedFood = createFood(addFoodRequestZonderPortions);

        String day = "2001-01-01";

        createLogEntry(day,savedFood, null, 1.0);

        // check correctly added:
        List<LogEntryDto> entriesForDay = getLogEntriesForDay(day);

        assertThat(entriesForDay).hasSize(1);
        assertThat(entriesForDay.get(0).getMeal()).isEqualTo("BREAKFAST");
        assertThat(entriesForDay.get(0).getMacrosCalculated()).isNotNull();
    }


    @Test
    public void testCreateLogEntryWithPortions() {

        // 1 create a food without portion:
        AddFoodRequest addFoodRequestMetPortions = AddFoodRequest.builder()
                .name("logEntryFood2")
                .carbs(1.0)
                .fat(2.0)
                .protein(3.0)
                .portions(Arrays.asList(
                        PortionDto.builder().grams(1.0).description("p1").build(),
                        PortionDto.builder().grams(2.0).description("p2").build()
                ))
                .build();
        FoodDto savedFood = createFood(addFoodRequestMetPortions);
        PortionDto portion1= savedFood.getPortions().stream().filter(p->p.getDescription().equals("p1")).findFirst().get();

        // Portion 1 3x
        String day = "2001-01-02";
        createLogEntry(day,savedFood, portion1.getId(), 3.0);

        // check correctly added:
        List<LogEntryDto> entriesForDay = getLogEntriesForDay(day);
        assertThat(entriesForDay).hasSize(1);
        assertThat(entriesForDay.get(0).getMeal()).isEqualTo("BREAKFAST");
        assertThat(entriesForDay.get(0).getMacrosCalculated()).isNotNull();
        assertThat(entriesForDay.get(0).getFood().getId()).isEqualTo(savedFood.getId());
        assertThat(entriesForDay.get(0).getPortion().getId()).isEqualTo(portion1.getId());
        assertThat(entriesForDay.get(0).getMultiplier()).isEqualTo(3.0);
        assertThat(entriesForDay.get(0).getMacrosCalculated()).isNotNull();
    }

    @Test
    public void testUpdateLogEntry(){

        String nameFood = "updateLogEntryFood";
        String day = "2001-01-03";

        // 1 create a food without portion:
        AddFoodRequest addFoodRequestZonderPortions = AddFoodRequest.builder().name(nameFood).carbs(1.0).fat(2.0).protein(3.0).build();
        FoodDto savedFood = createFood(addFoodRequestZonderPortions);

        createLogEntry(day,savedFood, null, 1.0);

        List<LogEntryDto> entriesForDay = getLogEntriesForDay(day);
        assertThat(entriesForDay).hasSize(1);

        // Create Store Request with update of entry
        List<StoreLogEntryRequest> updateEntries = Arrays.asList(
                StoreLogEntryRequest.builder()
                        .day(Date.valueOf(LocalDate.parse(day)))
                        .meal("BREAKFAST")
                        .portionId(null)
                        .foodId(savedFood.getId())
                        .multiplier(3.0)
                        .id(entriesForDay.get(0).getId())
                        .build()
        );
        ResponseEntity responseEntity = logEntryService.storeLogEntries(updateEntries);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value()); // why not CREATED?

        // Check log entry has been updated
        List<LogEntryDto> updatedForDay = getLogEntriesForDay(day);
        assertThat(updatedForDay).hasSize(1);
        assertThat(updatedForDay.size()).isEqualTo(1);
        assertThat(updatedForDay.get(0).getMultiplier()).isEqualTo(3.0);
        assertThat(updatedForDay.get(0).getId()).isEqualTo(entriesForDay.get(0).getId());
    }

    @Test
    public void testDeleteLogEntry(){

        String day = "2001-01-04";
        String nameFood = "foodDeleteLogEntry";

        // 1 create a food without portion:
        AddFoodRequest addFoodRequestZonderPortions = AddFoodRequest.builder().name(nameFood).carbs(1.0).fat(2.0).protein(3.0).build();
        FoodDto savedFood = createFood(addFoodRequestZonderPortions);


        createLogEntry(day,savedFood, null, 1.0);

        // check correctly added:
        List<LogEntryDto> entriesForDay = getLogEntriesForDay(day);
        assertThat(entriesForDay).hasSize(1);

        // delete the entry
        logEntryService.deleteLogEntry(entriesForDay.get(0).getId());
        List<LogEntryDto> afterDeleteForDay = getLogEntriesForDay(day);
        assertThat(afterDeleteForDay).hasSize(0);
    }




    private List<LogEntryDto> getLogEntriesForDay(String day) {
        ResponseEntity logEntriesForDay = logEntryService.getLogEntriesForDay(day);
        assertThat(logEntriesForDay.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value()); // why not CREATED?
        return (List<LogEntryDto>) logEntriesForDay.getBody();
    }
}
