package csl.servicetests;

import csl.database.model.Setting;
import csl.database.model.Weight;
import csl.dto.UserSettingsDto;
import csl.dto.WeightDto;
import csl.security.ThreadLocalHolder;
import csl.security.UserInfo;
import csl.servicetests.utils.AbstractApplicationIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WeightServiceITest extends AbstractApplicationIntegrationTest {

    private Integer userId;

    @BeforeAll
    public synchronized  void setUserContext() {

        synchronized (this) {
            if (this.userId == null) {
                log.debug("Creating test user for test " + this.getClass().getName());
                this.userId = createUser(this.getClass().getName());
            }
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(Integer.valueOf(this.userId));
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    public void testStoreAndUpdateAndDeleteWeight() { // Alle tests in 1 ivm multithreading. Anders extra filter nodig in de test :)

        List<WeightDto> weightEntries = getWeightEntries();
        assertThat(weightEntries).hasSize(0);

        // store weight:
        WeightDto newWeight = WeightDto.builder()
                .weight(10.0)
                .day(LocalDate.parse("1980-01-01"))
                .build();
        ResponseEntity responseEntity = weightService.storeWeightEntry(newWeight);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());

        weightEntries = getWeightEntries();
        assertThat(weightEntries).hasSize(1);
        assertThat(weightEntries.get(0).getWeight()).isEqualTo(10.0);
        assertThat(weightEntries.get(0).getDay()).isEqualTo(LocalDate.parse("1980-01-01"));

        // add another weight:
        WeightDto weight2 = WeightDto.builder()
                .weight(11.0)
                .day(LocalDate.parse("1980-01-02"))
                .build();
        responseEntity = weightService.storeWeightEntry(weight2);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());

        weightEntries = getWeightEntries();
        assertThat(weightEntries).hasSize(2);

        // add another weight on same day (adding for day already registred)
        WeightDto weight3 = WeightDto.builder()
                .weight(13.0)
                .day(LocalDate.parse("1980-01-02"))
                .build();
        responseEntity = weightService.storeWeightEntry(weight3);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());

        // weight has been updated:
        weightEntries = getWeightEntries();
        assertThat(weightEntries).hasSize(2);
        WeightDto weightDto = weightEntries.stream().filter(w -> w.getDay().equals(LocalDate.parse("1980-01-02"))).findFirst().get();
        assertThat(weightDto.getWeight()).isEqualTo(13.0);

        // update weight on same day (updating a weight) (update the weight only)
        WeightDto weight4 = WeightDto.builder()
                .weight(14.0)
                .id(weightDto.getId())
                .day(LocalDate.parse("1980-01-02"))
                .build();
        responseEntity = weightService.storeWeightEntry(weight4);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());

        // weight has been updated:
        weightEntries = getWeightEntries();
        assertThat(weightEntries).hasSize(2);
        assertThat(weightEntries.stream().filter(w->w.getDay().equals(LocalDate.parse("1980-01-02"))).findFirst().get().getWeight()).isEqualTo(14.0);

        // update weight on update the weight only and day
        WeightDto weight5 = WeightDto.builder()
                .weight(15.0)
                .id(weightDto.getId())
                .day(LocalDate.parse("1980-01-03"))
                .build();
        responseEntity = weightService.storeWeightEntry(weight5);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());

        // weight has been updated:
        weightEntries = getWeightEntries();
        assertThat(weightEntries).hasSize(2);
        assertThat(weightEntries.stream().filter(w->w.getDay().equals(LocalDate.parse("1980-01-03"))).findFirst().get().getWeight()).isEqualTo(15.0);

        // Delete it
        responseEntity = weightService.deleteWeightEntry(weightDto.getId());
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());

        // weight has been deleted:
        weightEntries = getWeightEntries();
        assertThat(weightEntries).hasSize(1);
    }

    private List<WeightDto> getWeightEntries() {
        ResponseEntity entity = weightService.getAllWeightEntries();
        assertThat(entity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        return (List<WeightDto>) entity.getBody();
    }
}
