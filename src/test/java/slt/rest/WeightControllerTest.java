package slt.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slt.dto.WeightDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.service.WeightService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WeightControllerTest {

    private WeightController controller;
    private WeightService service;

    @BeforeEach
    void setup() {
        service = mock(WeightService.class);
        controller = new WeightController(service);
        final var userInfo = new UserInfo();
        userInfo.setUserId(1L);
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    void getAllWeight() {
        final var weightId = 1L;
        final var weight = WeightDto.builder().id(weightId)
                .remark("remarkable")
                .weight(12.3)
                .day(LocalDate.of(2025, 1,1)).build();
        when(service.getAllWeights(1L)).thenReturn(List.of(weight));
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
