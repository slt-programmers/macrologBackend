package slt.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import slt.database.entities.Dish;
import slt.database.entities.Food;
import slt.database.entities.Ingredient;
import slt.database.entities.Portion;
import slt.dto.*;

import java.util.List;

public class DishMapperTest {

    private final DishMapper mapper = DishMapper.INSTANCE;

    @Test
    void mapToDishDtoWithoutIngredients() {
        final var dish = Dish.builder().id(1L)
                .name("dishname")
                .userId(2L)
                .build();
        final var dto = mapper.map(dish);
        Assertions.assertNotNull(dto);
        Assertions.assertEquals(1L, dto.getId());
        Assertions.assertEquals("dishname", dto.getName());
        Assertions.assertEquals(0, dto.getIngredients().size());
        Assertions.assertNotNull(dto.getMacrosCalculated());
        Assertions.assertEquals(0, dto.getMacrosCalculated().getProtein());
        Assertions.assertEquals(0, dto.getMacrosCalculated().getFat());
        Assertions.assertEquals(0, dto.getMacrosCalculated().getCarbs());
        Assertions.assertEquals(0, dto.getMacrosCalculated().getCalories());
    }

    @Test
    void mapToDishDtoWithIngredients() {
        final var portion = Portion.builder()
                .id(7L)
                .grams(75D)
                .description("desc")
                .build();
        final var food1 = Food.builder()
                .id(4L)
                .userId(2L)
                .name("food1")
                .protein(1D)
                .fat(2D)
                .carbs(3D)
                .build();
        final var food2 = Food.builder()
                .id(6L)
                .userId(2L)
                .protein(4D)
                .fat(5D)
                .carbs(6D)
                .portions(List.of(portion))
                .build();

        final var dish = Dish.builder().id(1L)
                .name("dishname")
                .userId(2L)
                .ingredients(List.of(Ingredient.builder()
                        .id(3L)
                        .food(food1)
                        .multiplier(1.5)
                        .build(), Ingredient.builder()
                        .id(5L)
                        .food(food2)
                        .portion(portion)
                        .multiplier(2.5)
                        .build()))
                .build();
        final var dto = mapper.map(dish);
        Assertions.assertNotNull(dto);
        Assertions.assertEquals(1L, dto.getId());
        Assertions.assertEquals("dishname", dto.getName());
        Assertions.assertEquals(2, dto.getIngredients().size());

        Assertions.assertNotNull(dto.getMacrosCalculated());
        Assertions.assertEquals(9.0, dto.getMacrosCalculated().getProtein());
        Assertions.assertEquals(12.375, dto.getMacrosCalculated().getFat());
        Assertions.assertEquals(15.75, dto.getMacrosCalculated().getCarbs());
        Assertions.assertEquals(210, dto.getMacrosCalculated().getCalories());
    }

    @Test
    void mapToDishWithoutIngredients() {
        final var dto = DishDto.builder()
                .id(1L)
                .name("dishname")
                .macrosCalculated(MacroDto.builder()
                        .protein(1D)
                        .fat(2D)
                        .carbs(3D)
                        .calories(50)
                        .build())
                .build();
        final var dish = mapper.map(dto, 2L);
        Assertions.assertNotNull(dish);
        Assertions.assertEquals(1L, dish.getId());
        Assertions.assertEquals(2L, dish.getUserId());
        Assertions.assertEquals("dishname", dish.getName());
        Assertions.assertEquals(0, dish.getIngredients().size());
    }


    @Test
    void mapToDishWithIngredients() {
        final var portion1 = PortionDto.builder()
                .id(5L)
                .grams(111D)
                .description("desc")
                .macros(MacroDto.builder()
                        .protein(3D)
                        .fat(6D)
                        .carbs(9D)
                        .calories(765)
                        .build())
                .build();
        final var food1 = FoodDto.builder()
                .id(4L)
                .protein(2D)
                .fat(4D)
                .carbs(6D)
                .portions(List.of(portion1))
                .name("food1").build();
        final var foodDto2 = FoodDto.builder()
                .id(6L)
                .protein(5D)
                .fat(6D)
                .carbs(7D)
                .name("food2").build();
        final var ingredientDto1 = IngredientDto.builder()
                .id(3L)
                .multiplier(1.5)
                .food(food1)
                .portion(portion1)
                .build();
        final var ingredientDto2 = IngredientDto.builder()
                .id(7L)
                .multiplier(2.5)
                .food(foodDto2)
                .build();
        final var dto = DishDto.builder()
                .id(1L)
                .name("dishname")
                .macrosCalculated(MacroDto.builder()
                        .protein(1D)
                        .fat(2D)
                        .carbs(3D)
                        .calories(50)
                        .build())
                .ingredients(List.of(ingredientDto1, ingredientDto2))
                .build();
        final var dish = mapper.map(dto, 2L);
        Assertions.assertNotNull(dish);
        Assertions.assertEquals(1L, dish.getId());
        Assertions.assertEquals(2L, dish.getUserId());
        Assertions.assertEquals("dishname", dish.getName());
        Assertions.assertEquals(2, dish.getIngredients().size());

        final var ingredient1 = dish.getIngredients().getFirst();
        Assertions.assertNotNull(ingredient1);
        Assertions.assertEquals(3L, ingredient1.getId());
        Assertions.assertEquals(1.5, ingredient1.getMultiplier());
        Assertions.assertNotNull(ingredient1.getFood());

        final var food = ingredient1.getFood();
        Assertions.assertEquals(4L, food.getId());
        Assertions.assertNull(food.getUserId());
        Assertions.assertEquals(2.0, food.getProtein());
        Assertions.assertEquals(4.0, food.getFat());
        Assertions.assertEquals(6.0, food.getCarbs());
        Assertions.assertEquals(1, food.getPortions().size());

        final var foodPortion = food.getPortions().getFirst();
        Assertions.assertEquals(5L, foodPortion.getId());
        Assertions.assertEquals(111L, foodPortion.getGrams());
        Assertions.assertEquals("desc", foodPortion.getDescription());
        final var ingredientPortion = ingredient1.getPortion();
        Assertions.assertNotNull(ingredientPortion);
        Assertions.assertEquals(5L, ingredientPortion.getId());
        Assertions.assertEquals("desc", ingredientPortion.getDescription());
        Assertions.assertEquals(111L, ingredientPortion.getGrams());
        Assertions.assertNull(ingredient1.getMealtime());

        final var ingredient2 = dish.getIngredients().getLast();
        Assertions.assertNotNull(ingredient2);
        Assertions.assertEquals(7L, ingredient2.getId());
        Assertions.assertEquals(2.5, ingredient2.getMultiplier());
        Assertions.assertNotNull(ingredient2.getFood());
        Assertions.assertNull(ingredient2.getPortion());

        final var food2 = ingredient2.getFood();
        Assertions.assertEquals(6L, food2.getId());
        Assertions.assertNull(food.getUserId());
        Assertions.assertEquals(5.0, food2.getProtein());
        Assertions.assertEquals(6.0, food2.getFat());
        Assertions.assertEquals(7.0, food2.getCarbs());
        Assertions.assertEquals(0, food2.getPortions().size());
    }

}
