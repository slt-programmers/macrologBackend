package slt.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slt.database.WeightRepository;
import slt.database.entities.Weight;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.service.WeightService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WeightControllerTest {

    private WeightController controller;
    private WeightRepository repository;

    @BeforeEach
    void setup() {
        repository = mock(WeightRepository.class);
        final var service = mock(WeightService.class);
        controller = new WeightController(repository, service);
        final var userInfo = new UserInfo();
        userInfo.setUserId(1L);
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    void getAllWeight() {
        final var weightId = 1L;
        final var weight = Weight.builder().id(weightId)
                .userId(1L)
                .remark("remarkable")
                .weight(12.3)
                .day(Date.valueOf("2025-01-01")).build();
        when(repository.getAllWeightEntries(1L)).thenReturn(List.of(weight));
        final var result = controller.getAllWeight();
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(1, result.getBody().size());
        final var firstWeight = result.getBody().getFirst();
        Assertions.assertEquals(weightId, firstWeight.getId());
        Assertions.assertEquals(LocalDate.parse("2025-01-01"), firstWeight.getDay());
        Assertions.assertEquals(12.3, firstWeight.getWeight());
        Assertions.assertEquals("remarkable",firstWeight.getRemark());
    }
}
