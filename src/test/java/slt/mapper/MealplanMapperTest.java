package slt.mapper;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import slt.database.entities.Mealplan;
import slt.database.entities.Mealtime;
import slt.dto.MealplanDto;
import slt.dto.MealtimeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
class MealplanMapperTest {

    private final MealplanMapper mapper = Mappers.getMapper(MealplanMapper.class);

    @Test
    void mapToEntity() {
        final var mealtimeDto = MealtimeDto.builder().build();
        final var mealplanId = 1L;
        final var mealplanDto = MealplanDto.builder().id(mealplanId).mealtimes(List.of(mealtimeDto)).build();
        final var userId = 123L;
        final var result = mapper.map(mealplanDto, userId);
        assertEquals(userId, result.getUserId());
        assertEquals(mealplanId, result.getMealtimes().getFirst().getMealplan().getId());
    }

    @Test
    void mapToDto() {
        final var mealtimeId = 2L;
        final var mealtime = Mealtime.builder().id(mealtimeId).build();
        final var mealplanId = 1L;
        final var mealplan = Mealplan.builder().id(mealplanId).mealtimes(List.of(mealtime)).build();
        final var result = mapper.map(mealplan);
        assertEquals(mealplanId, result.getId());
        assertEquals(mealtimeId, result.getMealtimes().getFirst().getId());
    }
}