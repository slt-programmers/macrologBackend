package csl.rest;

import csl.database.*;
import csl.database.model.Food;
import csl.database.model.Meal;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class AccountService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private LogEntryRepository logEntryRepository;

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private PortionRepository portionRepository;

    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private UserAcccountRepository userAcccountRepository;

    @Autowired
    private WeightRepository weightRepository;

    @Autowired
    private FoodRepository foodRepository;

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
