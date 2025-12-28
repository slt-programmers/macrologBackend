package slt.servicetests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import slt.database.entities.UserAccount;
import slt.dto.DishDto;
import slt.dto.FoodDto;
import slt.dto.IngredientDto;
import slt.dto.PortionDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.servicetests.utils.AbstractApplicationIntegrationTest;

import java.util.List;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccountServiceITest extends AbstractApplicationIntegrationTest {

    @Test
    void deleteAccount() {
        var userToBeDeleted = UserAccount.builder().isAdmin(false)
                .username("deleteAccountTest").password("testtest").email("test@test.nl").build();
        userToBeDeleted = userAccountRepository.saveAccount(userToBeDeleted);
        var allUsers = userAccountRepository.getAllUsers();
        Assertions.assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("deleteAccountTest")));

        ThreadLocalHolder.getThreadLocal().set(UserInfo.builder().userId(userToBeDeleted.getId()).build());

        final var foodDto = FoodDto.builder()
                .name("cheese")
                .protein(1D)
                .fat(2D)
                .carbs(3D)
                .portions(List.of(PortionDto.builder().description("desc").grams(10D).build()))
                .build();
        foodService.postFood(foodDto);
        var allFood = foodService.getAllFood();
        var allFoodForUser = allFood.getBody();
        Assertions.assertNotNull(allFoodForUser);
        Assertions.assertEquals(1, allFoodForUser.size());
        var allPortions = portionRepository.getAllPortions();
        final var portionsListSize = allPortions.size();

        final var dishDto = DishDto.builder()
                .name("dish")
                .ingredients(List.of(IngredientDto.builder()
                        .multiplier(2D)
                        .food(allFoodForUser.getFirst()).build()))
                .build();
        dishService.storeDish(dishDto);
        var allDishes = dishService.getAllDishes();
        var allDishesForUser = allDishes.getBody();
        Assertions.assertNotNull(allDishesForUser);
        Assertions.assertEquals(1, allFoodForUser.size());
        var allIngredients = ingredientRepository.getAllIngredients();
        final var ingredientsListSize = allIngredients.size();

        accountService.deleteAccount(userToBeDeleted.getId());

        allUsers = userAccountRepository.getAllUsers();
        Assertions.assertFalse(allUsers.stream().anyMatch(u -> u.getUsername().equals("deleteAccountTest")));
        allFood = foodService.getAllFood();
        allFoodForUser = allFood.getBody();
        Assertions.assertNotNull(allFoodForUser);
        Assertions.assertEquals(0, allFoodForUser.size());
        allPortions = portionRepository.getAllPortions();
        var newPortionsListSize = allPortions.size();
        newPortionsListSize++;
        Assertions.assertEquals(portionsListSize, newPortionsListSize);

        allDishes = dishService.getAllDishes();
        allDishesForUser = allDishes.getBody();
        Assertions.assertNotNull(allDishesForUser);
        Assertions.assertEquals(0, allFoodForUser.size());
        allIngredients = ingredientRepository.getAllIngredients();
        var newIngredientsListSize = allIngredients.size();
        newIngredientsListSize++;
        Assertions.assertEquals(ingredientsListSize, newIngredientsListSize);
    }
}
