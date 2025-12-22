package slt.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import slt.database.*;
import slt.database.entities.Food;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class AccountService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private LogEntryRepository logEntryRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private MealplanRepository mealplanRepository;

    @Autowired
    private PortionRepository portionRepository;

    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private WeightRepository weightRepository;

    @Autowired
    private FoodRepository foodRepository;

    void deleteAccount(final Long userId) {
        activityRepository.deleteAllForUser(userId);
        weightRepository.deleteAllForUser(userId);
        logEntryRepository.deleteAllForUser(userId);

        // TODO test if ingredients are properly being deleted
        dishRepository.deleteAllForUser(userId);
        mealplanRepository.deleteAllForUser(userId);

        // TODO test if portions can be deleted like ingredients, without this hastle
        List<Food> allFood = foodRepository.getAllFood(userId);
        List<Long> foodIds = allFood.stream().map(Food::getId).collect(toList());
        portionRepository.deleteAllForUser(foodIds);
        foodRepository.deleteAllForUser(userId);

        settingsRepository.deleteAllForUser(userId);
        userAccountRepository.deleteUser(userId);
    }

}
