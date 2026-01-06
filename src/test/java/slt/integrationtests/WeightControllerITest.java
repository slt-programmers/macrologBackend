package slt.integrationtests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;
import slt.dto.WeightDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.integrationtests.utils.AbstractApplicationIntegrationTest;

import java.time.LocalDate;
import java.util.List;

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
        final var userInfo = UserInfo.builder().userId(this.userId).build();
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    public void testPostAndUpdateAndDeleteWeight() { // Alle tests in 1 ivm multithreading. Anders extra filter nodig in de test :)
        var weightDtos = getWeightDtos();
        Assertions.assertEquals(0, weightDtos.size());

        // add new weight:
        final var newWeight = WeightDto.builder()
                .weight(10.0)
                .day(LocalDate.parse("1980-01-01"))
                .build();
        var responseEntity = weightController.postWeight(newWeight);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        weightDtos = getWeightDtos();
        Assertions.assertEquals(1, weightDtos.size());
        Assertions.assertEquals(10.0, weightDtos.getFirst().getWeight());
        Assertions.assertEquals(LocalDate.parse("1980-01-01"), weightDtos.getFirst().getDay());

        // add another new weight:
        final var weight2 = WeightDto.builder()
                .weight(11.0)
                .day(LocalDate.parse("1980-01-02"))
                .build();
        responseEntity = weightController.postWeight(weight2);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        weightDtos = getWeightDtos();
        Assertions.assertEquals(2, weightDtos.size());

        // add new weight on existing day
        final var weight3 = WeightDto.builder()
                .weight(13.0)
                .remark("first remark")
                .day(LocalDate.parse("1980-01-02"))
                .build();
        responseEntity = weightController.postWeight(weight3);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        weightDtos = getWeightDtos();
        Assertions.assertEquals(2, weightDtos.size());
        final var optionalWeightDto = weightDtos.stream().filter(w ->
                w.getDay().equals(LocalDate.parse("1980-01-02"))).findFirst();
        Assertions.assertTrue(optionalWeightDto.isPresent());
        final var weightDto = optionalWeightDto.get();
        Assertions.assertEquals(13.0, weightDto.getWeight());
        Assertions.assertEquals("first remark", weightDto.getRemark());

        // update weight on same day (updating a weight)
        final var weight4 = WeightDto.builder()
                .id(weightDto.getId())
                .weight(14.0)
                .remark("second remark")
                .day(LocalDate.parse("1980-01-02"))
                .build();
        responseEntity = weightController.postWeight(weight4);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // weight has been updated:
        weightDtos = getWeightDtos();
        Assertions.assertEquals(2, weightDtos.size());
        final var optionalWeightDto2 = weightDtos.stream().filter(w -> w.getDay().equals(LocalDate.parse("1980-01-02")))
                .findFirst();
        Assertions.assertTrue(optionalWeightDto2.isPresent());
        final var weightDto2 = optionalWeightDto2.get();
        Assertions.assertEquals(14.0, weightDto2.getWeight());
        Assertions.assertEquals("second remark", weightDto2.getRemark());

        // update weight and day on existing entity
        var weight5 = WeightDto.builder()
                .id(weightDto.getId())
                .weight(15.0)
                .day(LocalDate.parse("1980-01-03"))
                .build();
        responseEntity = weightController.postWeight(weight5);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // weight has been updated:
        weightDtos = getWeightDtos();
        Assertions.assertEquals(2, weightDtos.size());
        final var optionalWeight5 = weightDtos.stream().filter(w ->
                w.getDay().equals(LocalDate.parse("1980-01-03"))).findFirst();
        Assertions.assertTrue(optionalWeight5.isPresent());
        weight5 = optionalWeight5.get();
        Assertions.assertEquals(15.0, weight5.getWeight());

        // update weight and day on existing entity with existing day
        final var weight6 = WeightDto.builder()
                .id(weightDto.getId())
                .weight(16.0)
                .day(LocalDate.parse("1980-01-01"))
                .build();
        responseEntity = weightController.postWeight(weight6);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // weight has been updated:
        weightDtos = getWeightDtos();
        Assertions.assertEquals(2, weightDtos.size());
        final var optionalWeight6 = weightDtos.stream().filter(w ->
                w.getDay().equals(LocalDate.parse("1980-01-01"))).findFirst();
        Assertions.assertTrue(optionalWeight6.isPresent());
        Assertions.assertEquals(16.0, optionalWeight6.get().getWeight());

        // Delete it
        final var deleteResponseEntity = weightController.deleteWeightEntry(weightDto.getId());
        Assertions.assertEquals(HttpStatus.OK, deleteResponseEntity.getStatusCode());

        // weight has been deleted:
        weightDtos = getWeightDtos();
        Assertions.assertEquals(1, weightDtos.size());
    }

    private List<WeightDto> getWeightDtos() {
        final var response = weightController.getAllWeight();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody();
    }
}
