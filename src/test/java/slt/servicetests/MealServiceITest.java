package slt.servicetests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import slt.dto.*;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.servicetests.utils.AbstractApplicationIntegrationTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MealServiceITest extends AbstractApplicationIntegrationTest {

    private Integer userId;

    @BeforeAll
    public void setUserContext() {

        if (this.userId == null) {
            log.debug("Creating test user for test " + this.getClass().getName());
            this.userId = createUser(this.getClass().getName());
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(Integer.valueOf(this.userId));
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    public void testCreateAndDeleteEmptyMeal(){

        String mealName = "emptyMeal";
        AddMealRequest newMealRequest = AddMealRequest.builder().name(mealName).build();
        ResponseEntity responseEntity = mealService.storeMeal(newMealRequest);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

        ResponseEntity<List<MealDto>> allMeals = mealService.getAllMeals();
        List<MealDto> foundMeals = allMeals.getBody();
        Optional<MealDto> emptyMeal = foundMeals.stream().filter(m -> m.getName().equals(mealName)).findFirst();
        assertThat(emptyMeal.isPresent()).isTrue();
        assertThat(emptyMeal.get().getId()).isNotNull();
        assertThat(emptyMeal.get().getIngredients()).isEmpty();

        responseEntity = mealService.deleteMeal(emptyMeal.get().getId());
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void testCreateSameMeal(){

        String mealName = "sameMeal";
        AddMealRequest newMeal = AddMealRequest.builder().name(mealName).build();
        ResponseEntity responseEntity = mealService.storeMeal(newMeal);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

        ResponseEntity<List<MealDto>> allMeals = mealService.getAllMeals();
        List<MealDto> foundMeals = allMeals.getBody();
        Optional<MealDto> emptyMeal = foundMeals.stream().filter(m -> m.getName().equals(mealName)).findFirst();
        assertThat(emptyMeal.isPresent()).isTrue();
        assertThat(emptyMeal.get().getId()).isNotNull();
        assertThat(emptyMeal.get().getIngredients()).isEmpty();

        AddMealRequest sameMeal = AddMealRequest.builder().name(mealName).build();
        responseEntity = mealService.storeMeal(sameMeal);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void testCreateMealWithMultipleFood(){

        String mealName = "with1Food";

        FoodDto food1 = createFood(AddFoodRequest.builder().name("food1").carbs(1.0).fat(2.0).protein(3.0).build());
        FoodDto food2 = createFood(AddFoodRequest.builder().name("food2").carbs(4.0).fat(5.0).protein(6.0).build());

        AddMealRequest newMeal = AddMealRequest.builder()
                .name(mealName)
                .ingredients(
                        Arrays.asList(
                                AddMealIngredientDto.builder().foodId(food1.getId()).multiplier(1.0).build(),
                                AddMealIngredientDto.builder().foodId(food2.getId()).multiplier(3.0).build()
                        )
                )
                .build();

        ResponseEntity responseEntity = mealService.storeMeal(newMeal);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

        ResponseEntity<List<MealDto>> allMeals = mealService.getAllMeals();
        List<MealDto> foundMeals = allMeals.getBody();
        Optional<MealDto> emptyMeal = foundMeals.stream().filter(m -> m.getName().equals(mealName)).findFirst();
        assertThat(emptyMeal.isPresent()).isTrue();
        assertThat(emptyMeal.get().getId()).isNotNull();
        assertThat(emptyMeal.get().getIngredients()).hasSize(2);

        assertThat(emptyMeal.get().getIngredients().get(0).getFood()).isNotNull();
        assertThat(emptyMeal.get().getIngredients().get(1).getFood()).isNotNull();

        Optional<IngredientDto> food1Optional = emptyMeal.get().getIngredients().stream().filter(i -> i.getFood().getName().equals("food1")).findFirst();
        Optional<IngredientDto> food2Optional = emptyMeal.get().getIngredients().stream().filter(i -> i.getFood().getName().equals("food2")).findFirst();

        assertThat(food1Optional.get().getMultiplier()).isEqualTo(1.0);
        assertThat(food2Optional.get().getMultiplier()).isEqualTo(3.0);

        responseEntity = mealService.deleteMeal(emptyMeal.get().getId());

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void testCreateMealFoodWithPortion(){

        String mealName = "withPortions";

        FoodDto food1 = createFood(AddFoodRequest.builder().name("food3").carbs(1.0).fat(2.0).protein(3.0).
                portions(Arrays.asList(
                        PortionDto.builder().description("p1").grams(200.0).build(),
                        PortionDto.builder().description("p2").grams(600.0).build()
                )).build());
        FoodDto food2 = createFood(AddFoodRequest.builder().name("food4").carbs(4.0).fat(5.0).protein(6.0).build());

        PortionDto food1Portion1 = food1.getPortions().stream().filter(p -> p.getDescription().equals("p1")).findFirst().get();
        AddMealRequest newMeal = AddMealRequest.builder()
                .name(mealName)
                .ingredients(
                        Arrays.asList(
                                AddMealIngredientDto.builder()
                                        .foodId(food1.getId())
                                        .portionId(food1Portion1.getId())
                                        .multiplier(1.0)
                                        .build(),
                                AddMealIngredientDto.builder().foodId(food2.getId()).multiplier(3.0).build()
                        )
                )
                .build();

        ResponseEntity responseEntity = mealService.storeMeal(newMeal);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

        ResponseEntity<List<MealDto>> allMeals = mealService.getAllMeals();
        List<MealDto> foundMeals = allMeals.getBody();
        Optional<MealDto> emptyMeal = foundMeals.stream().filter(m -> m.getName().equals(mealName)).findFirst();
        assertThat(emptyMeal.isPresent()).isTrue();
        assertThat(emptyMeal.get().getId()).isNotNull();
        assertThat(emptyMeal.get().getIngredients()).hasSize(2);

        assertThat(emptyMeal.get().getIngredients().get(0).getFood()).isNotNull();
        assertThat(emptyMeal.get().getIngredients().get(1).getFood()).isNotNull();

        Optional<IngredientDto> ingredient1Food1 = emptyMeal.get().getIngredients().stream().filter(i -> i.getFood().getName().equals("food3")).findFirst();
        assertThat(ingredient1Food1.isPresent()).isTrue();

        assertThat(ingredient1Food1.get().getMultiplier()).isEqualTo(1.0);
        assertThat(ingredient1Food1.get().getPortionId()).isEqualTo(food1Portion1.getId());
        // check portion exists with food:
        assertThat(ingredient1Food1.get().getFood().getPortions().stream().filter(p->p.getId().equals(ingredient1Food1.get().getPortionId())).findFirst().isPresent()).isTrue();

        responseEntity = mealService.deleteMeal(emptyMeal.get().getId());

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
    }


}
