package slt.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import slt.dto.WeightDto;

import java.sql.Date;
import java.time.LocalDate;

public class WeightMapperTest {

    private final WeightMapper mapper = WeightMapper.INSTANCE;

    @Test
    void testMapToWeight() {
        final var date = LocalDate.now();
        final var dto = WeightDto.builder()
                .id(2L)
                .day(date)
                .weight(123D)
                .remark("remark")
                .build();
        final var result = mapper.map(dto, 1L);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1L, result.getUserId());
        Assertions.assertEquals(2L, result.getId());
        Assertions.assertEquals(123D, result.getWeight());
        Assertions.assertEquals("remark", result.getRemark());
        Assertions.assertEquals(Date.valueOf(date), result.getDay());
    }

    @Test
    void testMapToWeightWithoutDay() {
        final var date = LocalDate.now();
        final var dto = WeightDto.builder()
                .id(2L)
                .weight(123D)
                .remark("remark")
                .build();
        final var result = mapper.map(dto, 1L);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1L, result.getUserId());
        Assertions.assertEquals(2L, result.getId());
        Assertions.assertEquals(123D, result.getWeight());
        Assertions.assertEquals("remark", result.getRemark());
        Assertions.assertEquals(Date.valueOf(date), result.getDay());
    }
}
