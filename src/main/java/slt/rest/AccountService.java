package slt.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import slt.database.*;
import slt.database.entities.Food;
import slt.database.entities.Meal;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class AccountService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private LogEntryRepository logEntryRepository;

    @Autowired
    private MealRepository mealRepository;

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

    void deleteAccount(Integer userId) {
        activityRepository.deleteAllForUser(userId);
        weightRepository.deleteAllForUser(userId);
        logEntryRepository.deleteAllForUser(userId);

        mealRepository.deleteAllForUser(userId);

        List<Food> allFood = foodRepository.getAllFood(userId);
        List<Integer> foodIds = allFood.stream().map(f->f.getId().intValue()).collect(toList());
        portionRepository.deleteAllForUser(foodIds);
        foodRepository.deleteAllForUser(userId);

        settingsRepository.deleteAllForUser(userId);
        userAccountRepository.deleteUser(userId);
    }

}
