package slt.servicetests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;
import slt.dto.WeightDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.servicetests.utils.AbstractApplicationIntegrationTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WeightControllerITest extends AbstractApplicationIntegrationTest {

    private Long userId;

    @BeforeAll
    public synchronized void setUserContext() {
        synchronized (this) {
            if (this.userId == null) {
                log.debug("Creating test user for test {}", this.getClass().getName());
                this.userId = createUser(this.getClass().getName());
            }
        }
        final var userInfo = new UserInfo();
        userInfo.setUserId(this.userId);
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    public void testPostAndUpdateAndDeleteWeight() { // Alle tests in 1 ivm multithreading. Anders extra filter nodig in de test :)
        var weightDtos = getWeightDtos();
        assertThat(weightDtos).hasSize(0);

        // add new weight:
        final var newWeight = WeightDto.builder()
                .weight(10.0)
                .day(LocalDate.parse("1980-01-01"))
                .build();
        var responseEntity = weightController.postWeight(newWeight);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        weightDtos = getWeightDtos();
        assertThat(weightDtos).hasSize(1);
        assertThat(weightDtos.getFirst().getWeight()).isEqualTo(10.0);
        assertThat(weightDtos.getFirst().getDay()).isEqualTo(LocalDate.parse("1980-01-01"));

        // add another new weight:
        final var weight2 = WeightDto.builder()
                .weight(11.0)
                .day(LocalDate.parse("1980-01-02"))
                .build();
        responseEntity = weightController.postWeight(weight2);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        weightDtos = getWeightDtos();
        assertThat(weightDtos).hasSize(2);

        // add new weight on existing day
        final var weight3 = WeightDto.builder()
                .weight(13.0)
                .remark("first remark")
                .day(LocalDate.parse("1980-01-02"))
                .build();
        responseEntity = weightController.postWeight(weight3);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        weightDtos = getWeightDtos();
        assertThat(weightDtos).hasSize(2);
        final var weightDto = weightDtos.stream().filter(w ->
                w.getDay().equals(LocalDate.parse("1980-01-02"))).findFirst().get();
        assertThat(weightDto.getWeight()).isEqualTo(13.0);
        assertThat(weightDto.getRemark()).isEqualTo("first remark");

        // update weight on same day (updating a weight)
        final var weight4 = WeightDto.builder()
                .id(weightDto.getId())
                .weight(14.0)
                .remark("second remark")
                .day(LocalDate.parse("1980-01-02"))
                .build();
        responseEntity = weightController.postWeight(weight4);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        // weight has been updated:
        weightDtos = getWeightDtos();
        assertThat(weightDtos).hasSize(2);
        final var weightDto2 = weightDtos.stream().filter(w -> w.getDay().equals(LocalDate.parse("1980-01-02")))
                .findFirst().get();
        assertThat(weightDto2.getWeight()).isEqualTo(14.0);
        assertThat(weightDto2.getRemark()).isEqualTo("second remark");

        // update weight and day on existing entity
        WeightDto weight5 = WeightDto.builder()
                .id(weightDto.getId())
                .weight(15.0)
                .day(LocalDate.parse("1980-01-03"))
                .build();
        responseEntity = weightController.postWeight(weight5);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        // weight has been updated:
        weightDtos = getWeightDtos();
        assertThat(weightDtos).hasSize(2);
        assertThat(weightDtos.stream().filter(w ->
                w.getDay().equals(LocalDate.parse("1980-01-03"))).findFirst().get().getWeight())
                .isEqualTo(15.0);

        // update weight and day on existing entity with existing day
        WeightDto weight6 = WeightDto.builder()
                .id(weightDto.getId())
                .weight(16.0)
                .day(LocalDate.parse("1980-01-01"))
                .build();
        responseEntity = weightController.postWeight(weight6);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        // weight has been updated:
        weightDtos = getWeightDtos();
        assertThat(weightDtos).hasSize(2);
        assertThat(weightDtos.stream().filter(w ->
                w.getDay().equals(LocalDate.parse("1980-01-01"))).findFirst().get().getWeight())
                .isEqualTo(16.0);

        // Delete it
        final var deleteResponseEntity = weightController.deleteWeightEntry(weightDto.getId());
        assertThat(deleteResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        // weight has been deleted:
        weightDtos = getWeightDtos();
        assertThat(weightDtos).hasSize(1);
    }

    private List<WeightDto> getWeightDtos() {
        final var response = weightController.getAllWeight();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }
}
