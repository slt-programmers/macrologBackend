package slt.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import slt.database.entities.*;
import slt.dto.Meal;
import slt.dto.Weekday;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

//@DataJpaTest // Used for MealplanCrudRepository
@SpringBootTest
@ActiveProfiles("test")
class MealplanRepositoryTest {

    @Autowired
    MealplanRepository repository;

    @Autowired
    FoodRepository foodRepository;

    @Autowired
    UserAccountRepository userAccountRepository;

    @BeforeEach
    void setup() {
        final var user = new UserAccount();
        user.setEmail("email@test.nl");
        user.setAdmin(false);
        user.setUserName("test");
        user.setPassword("xyz");
        userAccountRepository.saveAccount(user);
        final var food =  Food.builder()
                .name("food")
                .protein(1D)
                .fat(2D)
                .carbs(3D)
                .userId(1L)
                .build();
        foodRepository.saveFood(food);
    }

    @Test
    void saveMealplan() {
        final var food = foodRepository.getAllFood(1L).getFirst();
        final var mealplan = Mealplan.builder()
                .userId(1L)
                .title("my plan").build();
        final var mealtime = Mealtime.builder()
                .mealplan(mealplan)
                .meal(Meal.BREAKFAST.toString())
                .weekday(Weekday.MONDAY.getLabel())
                .build();
        final var dish = new Dish();
        dish.setId(1L);
        final var ingredient = Ingredient.builder()
                .mealtime(mealtime)
                .food(food)
                .multiplier(1D).build();
        mealtime.setIngredients(List.of(ingredient));
        mealplan.setMealtimes(List.of(mealtime));

        final var savedPlan = repository.saveMealplan(mealplan);
        final var foundPlan = repository.getAllMealplans(1L).getFirst();
        assertEquals(savedPlan.getId(), foundPlan.getId());
        assertEquals(savedPlan.getTitle(), foundPlan.getTitle());
        assertEquals(savedPlan.getUserId(), foundPlan.getUserId());
        assertEquals(savedPlan.getMealtimes().size(), foundPlan.getMealtimes().size());
        final var savedMealtime = savedPlan.getMealtimes().getFirst();
        final var foundMealtime = foundPlan.getMealtimes().getFirst();
        assertEquals(savedMealtime.getId(), foundMealtime.getId());
        assertEquals(savedMealtime.getMeal(), foundMealtime.getMeal());
        assertEquals(savedMealtime.getWeekday(), foundMealtime.getWeekday());
        assertEquals(savedMealtime.getIngredients().size(), foundMealtime.getIngredients().size());
        final var savedIngredient = savedMealtime.getIngredients().getFirst();
        final var foundIngredient = foundMealtime.getIngredients().getFirst();
        assertEquals(savedIngredient.getId(), foundIngredient.getId());
        assertEquals(savedIngredient.getFood().getId(), foundIngredient.getFood().getId());
        assertEquals(savedIngredient.getMultiplier(), foundIngredient.getMultiplier());
    }

    @Test
    void deleteMealplan() {
    }

    @Test
    void getAllMealplans() {
    }

    @Test
    void deleteAllForUser() {
    }
}