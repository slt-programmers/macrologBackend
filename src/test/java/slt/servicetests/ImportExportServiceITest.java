package slt.servicetests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
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
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ImportExportServiceITest extends AbstractApplicationIntegrationTest {

    private Long userId;

    @BeforeEach
    public void setUserContext() {

        if (this.userId == null) {
            log.debug("Creating test user for test " + this.getClass());
            this.userId = createUser("logEntryUser");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(this.userId);
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    public void testItAll(){

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
        PortionDto portion1= savedFood.getPortions().stream().filter(p->p.getDescription().equals("portion1")).findFirst().get();

        // add log entry without portion
        String day = "2001-01-02";
        createLogEntry(day,foodZonderPortion, null, 3.0);

        // add log entry with portion
        createLogEntry(day,savedFood, portion1, 3.0);

        // add activity
        List<LogActivityDto> newActivities = Arrays.asList(
                LogActivityDto.builder()
                        .day(Date.valueOf(LocalDate.parse("2003-01-01" )))
                        .name("Running")
                        .calories(20.0)
                        .build(),
                LogActivityDto.builder()
                        .day(Date.valueOf(LocalDate.parse("2003-01-01" )))
                        .name("Cycling")
                        .calories(30.0)
                        .build()

        );
        ResponseEntity responseEntity = activityService.postActivities("2003-01-01" , newActivities);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        // add weight
        // store weight:
        WeightDto newWeight = WeightDto.builder()
                .weight(10.0)
                .day(LocalDate.parse("1980-01-01"))
                .build();
        responseEntity = weightController.postWeight(newWeight);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        // add settings:
        storeSetting("export1", "export1value");


        ResponseEntity exportEntity = exportService.getAll();
        assertThat(exportEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Export export = (Export) exportEntity.getBody();

        assertThat(export.getAllFood()).hasSize(2);
        assertThat(export.getAllFood().stream().filter(f-> f.getName().equals("exportFoodWithPortion")).findFirst().get().getPortions()).hasSize(2);
        assertThat(export.getAllFood().stream().filter(f-> f.getName().equals("exportFoodNoPortion")).findFirst().get().getPortions()).hasSize(0);

        assertThat(export.getAllLogEntries()).hasSize(1);
        assertThat(export.getAllActivities()).hasSize(2);
        assertThat(export.getAllWeights()).hasSize(1);
        assertThat(export.getAllSettingDtos()).hasSize(1);

        ResponseEntity importEntity = importService.setAll(export);
        assertThat(importEntity.getStatusCode()).isEqualTo(HttpStatus.OK); // why not CREATED?

    }

}
