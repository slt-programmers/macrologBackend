package slt.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Mealplan;
import slt.database.entities.Mealtime;
import slt.dto.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    void mapToEntityWithIngredient() {
        final var portionDto = PortionDto.builder().id(11L).grams(234D).description("desc").build();
        final var ingredientDto = IngredientDto.builder()
                .multiplier(1.1)
                .food(FoodDto.builder().id(2L).protein(1D).fat(2D).carbs(3D).name("food1").portions(List.of(portionDto)).build())
                .build();
        final var mealtimeDto = MealtimeDto.builder().ingredients(List.of(ingredientDto)).build();
        final var mealplanId = 1L;
        final var mealplanDto = MealplanDto.builder().id(mealplanId).mealtimes(List.of(mealtimeDto)).build();
        final var userId = 123L;
        final var result = mapper.map(mealplanDto, userId);
        assertEquals(userId, result.getUserId());
        assertEquals(mealplanId, result.getMealtimes().getFirst().getMealplan().getId());
        assertNotNull(result.getMealtimes().getFirst().getIngredients().getFirst().getMealtime());
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