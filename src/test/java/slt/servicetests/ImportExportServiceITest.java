package slt.servicetests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import slt.dto.AddFoodRequest;
import slt.dto.Export;
import slt.dto.FoodDto;
import slt.dto.PortionDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.servicetests.utils.AbstractApplicationIntegrationTest;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ImportExportServiceITest extends AbstractApplicationIntegrationTest {

    private Integer userId;

    @BeforeEach
    public void setUserContext() {

        if (this.userId == null) {
            log.debug("Creating test user for test " + this.getClass());
            Integer activityUser = createUser("logEntryUser");
            this.userId = activityUser;
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(Integer.valueOf(this.userId));
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    public void testItAll(){

        // 1 create data
        AddFoodRequest addFoodRequestMetPortions = AddFoodRequest.builder()
                .name("exportFood")
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

        FoodDto savedFood = createFood(addFoodRequestMetPortions);
        PortionDto portion1= savedFood.getPortions().stream().filter(p->p.getDescription().equals("portion1")).findFirst().get();

        String day = "2001-01-02";
        createLogEntry(day,savedFood, portion1.getId(), 3.0);

        ResponseEntity exportEntity = exportService.getAll();
        assertThat(exportEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value()); // why not CREATED?
        Export export = (Export) exportEntity.getBody();

        assertThat(export.getAllFood()).hasSize(1);
        assertThat(export.getAllFood().get(0).getPortions()).hasSize(2);

        assertThat(export.getAllLogEntries()).hasSize(1);


        ResponseEntity importEntity = importService.setAll(export);
        assertThat(importEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value()); // why not CREATED?

    }

}
