package slt.integrationtests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;
import slt.dto.*;
import slt.exceptions.ValidationException;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.integrationtests.utils.AbstractApplicationIntegrationTest;

import java.util.Arrays;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DishControllerITest extends AbstractApplicationIntegrationTest {

    private Long userId;

    @BeforeAll
    public void setUserContext() {
        if (this.userId == null) {
            log.debug("Creating test user for test {}", this.getClass().getName());
            this.userId = createUser(this.getClass().getName());
        }
        final var userInfo = UserInfo.builder().userId(this.userId).build();
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    public void testCreateAndDeleteEmptyDish() {
        final var dishName = "emptyDish";
        final var dishDto = DishDto.builder().name(dishName).build();
        final var responseEntity = dishController.postDish(dishDto);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        final var responseEntity2 = dishController.getAllDishes();
        Assertions.assertEquals(HttpStatus.OK, responseEntity2.getStatusCode());

        final var foundDishes = responseEntity2.getBody();
        Assertions.assertNotNull(foundDishes);
        final var emptyDish = foundDishes.stream().filter(m -> m.getName().equals(dishName)).findFirst();
        Assertions.assertTrue(emptyDish.isPresent());
        Assertions.assertNotNull(emptyDish.get().getId());
        Assertions.assertTrue(emptyDish.get().getIngredients().isEmpty());

        final var responseEntity3 = dishController.deleteDish(emptyDish.get().getId());
        Assertions.assertEquals(HttpStatus.OK, responseEntity3.getStatusCode());
    }

    @Test
    public void testCreateSameDish() {
        final var dishName = "sameDish";
        final var newDish = DishDto.builder().name(dishName).build();
        final var responseEntity = dishController.postDish(newDish);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        final var responseEntity2 = dishController.getAllDishes();
        final var foundDishes = responseEntity2.getBody();
        Assertions.assertNotNull(foundDishes);
        final var emptyDish = foundDishes.stream().filter(m -> m.getName().equals(dishName)).findFirst();
        Assertions.assertTrue(emptyDish.isPresent());
        Assertions.assertNotNull(emptyDish.get().getId());
        Assertions.assertTrue(emptyDish.get().getIngredients().isEmpty());

        final var sameDish = DishDto.builder().name(dishName).build();
        Assertions.assertThrows(ValidationException.class, () -> dishController.postDish(sameDish));
    }

    @Test
    public void testCreateDishWithMultipleFood() {
        final var dishName = "with1Food";
        final var food1 = createFood(FoodDto.builder().name("food1").carbs(1.0).fat(2.0).protein(3.0).build());
        final var food2 = createFood(FoodDto.builder().name("food2").carbs(4.0).fat(5.0).protein(6.0).build());

        final var newDish = DishDto.builder()
                .name(dishName)
                .ingredients(
                        Arrays.asList(
                                IngredientDto.builder().food(food1).multiplier(1.0).build(),
                                IngredientDto.builder().food(food2).multiplier(3.0).build()
                        )
                )
                .build();

        final var responseEntity = dishController.postDish(newDish);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        final var responseEntity1 = dishController.getAllDishes();
        final var foundDishes = responseEntity1.getBody();
        Assertions.assertNotNull(foundDishes);
        final var matchedDish = foundDishes.stream().filter(m -> m.getName().equals(dishName)).findFirst();
        Assertions.assertTrue(matchedDish.isPresent());
        Assertions.assertNotNull(matchedDish.get().getId());
        Assertions.assertEquals(2, matchedDish.get().getIngredients().size());
        Assertions.assertNotNull(matchedDish.get().getIngredients().get(0).getFood());
        Assertions.assertNotNull(matchedDish.get().getIngredients().get(1).getFood());
        final var food1Optional = matchedDish.get().getIngredients().stream()
                .filter(i -> i.getFood().getName().equals("food1")).findFirst();
        final var food2Optional = matchedDish.get().getIngredients().stream()
                .filter(i -> i.getFood().getName().equals("food2")).findFirst();
        Assertions.assertTrue(food1Optional.isPresent());
        Assertions.assertEquals(1.0, food1Optional.get().getMultiplier());
        Assertions.assertTrue(food2Optional.isPresent());
        Assertions.assertEquals(3.0, food2Optional.get().getMultiplier());

        final var responseEntity2 = dishController.deleteDish(matchedDish.get().getId());
        Assertions.assertEquals(HttpStatus.OK, responseEntity2.getStatusCode());
    }

    @Test
    public void testCreateDishFoodWithPortion() {
        final var dishName = "withPortions";
        final var food1 = createFood(FoodDto.builder().name("food3").carbs(1.0).fat(2.0).protein(3.0).
                portions(Arrays.asList(
                        PortionDto.builder().description("p1").grams(200.0).build(),
                        PortionDto.builder().description("p2").grams(600.0).build()
                )).build());
        final var food2 = createFood(FoodDto.builder().name("food4").carbs(4.0).fat(5.0).protein(6.0).build());
        final var optionalFood1Portion1 = food1.getPortions().stream().filter(p -> p.getDescription().equals("p1")).findFirst();
        Assertions.assertTrue(optionalFood1Portion1.isPresent());
        final var food1Portion1 = optionalFood1Portion1.get();
        final var newDish = DishDto.builder()
                .name(dishName)
                .ingredients(
                        Arrays.asList(
                                IngredientDto.builder()
                                        .food(food1)
                                        .portion(food1Portion1)
                                        .multiplier(1.0)
                                        .build(),
                                IngredientDto.builder().food(food2).multiplier(3.0).build())
                ).build();

        final var responseEntity = dishController.postDish(newDish);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        final var responseEntity1 = dishController.getAllDishes();
        final var foundDishes = responseEntity1.getBody();
        Assertions.assertNotNull(foundDishes);
        final var matchedDish = foundDishes.stream().filter(m -> m.getName().equals(dishName)).findFirst();
        Assertions.assertTrue(matchedDish.isPresent());
        Assertions.assertNotNull(matchedDish.get().getId());
        Assertions.assertEquals(2, matchedDish.get().getIngredients().size());
        Assertions.assertNotNull(matchedDish.get().getIngredients().get(0).getFood());
        Assertions.assertNotNull(matchedDish.get().getIngredients().get(1).getFood());

        final var ingredient1Food1 = matchedDish.get().getIngredients().stream()
                .filter(i -> i.getFood().getName().equals("food3")).findFirst();
        Assertions.assertTrue(ingredient1Food1.isPresent());
        Assertions.assertEquals(1.0, ingredient1Food1.get().getMultiplier());
        Assertions.assertEquals(food1Portion1.getId(), ingredient1Food1.get().getPortion().getId());
        final var responseEntity2 = dishController.deleteDish(matchedDish.get().getId());
        Assertions.assertEquals(HttpStatus.OK, responseEntity2.getStatusCode());
    }

}
