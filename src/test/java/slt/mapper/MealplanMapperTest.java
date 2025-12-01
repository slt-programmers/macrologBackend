package slt.mapper;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import slt.dto.MealplanDto;
import slt.dto.MealtimeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
class MealplanMapperTest {

    private final MealplanMapper mapper = Mappers.getMapper(MealplanMapper.class);

    @Test
    void map() {
        final var mealtimeDto = MealtimeDto.builder().build();
        final var mealplanId = 1L;
        final var mealplanDto = MealplanDto.builder().id(mealplanId).mealtimes(List.of(mealtimeDto)).build();
        final var userId = 123;
        final var result = mapper.map(mealplanDto, userId);
        assertEquals(userId, result.getUserId());
        assertEquals(mealplanId, result.getMealtimes().getFirst().getMealplan().getId());
    }
}