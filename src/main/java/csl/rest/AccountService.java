package csl.rest;

import csl.database.*;
import csl.database.model.Food;
import csl.database.model.Meal;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class AccountService {

    private ActivityRepository activityRepository = new ActivityRepository();
    private FoodRepository foodRepository = new FoodRepository();
    private IngredientRepository ingredientRepository = new IngredientRepository();
    private LogEntryRepository logEntryRepository = new LogEntryRepository();
    private MealRepository mealRepository = new MealRepository();
    private PortionRepository portionRepository = new PortionRepository();
    private SettingsRepository settingsRepository = new SettingsRepository();
    private UserAcccountRepository userAcccountRepository = new UserAcccountRepository();
    private WeightRepository weightRepository = new WeightRepository();

    void deleteAccount(Integer userId) {
        activityRepository.deleteAllForUser(userId);
        weightRepository.deleteAllForUser(userId);
        logEntryRepository.deleteAllForUser(userId);

        List<Meal> meals = mealRepository.getAllMeals(userId);
        List<Long> mealIds = meals.stream().map(Meal::getId).collect(toList());
        ingredientRepository.deleteAllForUser(mealIds);
        mealRepository.deleteAllForUser(userId);

        List<Food> allFood = foodRepository.getAllFood(userId);
        List<Long> foodIds = allFood.stream().map(Food::getId).collect(toList());
        portionRepository.deleteAllForUser(foodIds);
        foodRepository.deleteAllForUser(userId);

        settingsRepository.deleteAllForUser(userId);
        userAcccountRepository.deleteUser(userId);
    }

}
