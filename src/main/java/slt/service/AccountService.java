package slt.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import slt.database.*;

@Service
@AllArgsConstructor
public class AccountService {

    private ActivityRepository activityRepository;
    private LogEntryRepository logEntryRepository;
    private DishRepository dishRepository;
    private MealplanRepository mealplanRepository;
    private SettingsRepository settingsRepository;
    private UserAccountRepository userAccountRepository;
    private WeightRepository weightRepository;
    private FoodRepository foodRepository;

    public void deleteAccount(final Long userId) {
        activityRepository.deleteAllForUser(userId);
        logEntryRepository.deleteAllForUser(userId);
        dishRepository.deleteAllForUser(userId);
        mealplanRepository.deleteAllForUser(userId);
        foodRepository.deleteAllForUser(userId);
        weightRepository.deleteAllForUser(userId);
        settingsRepository.deleteAllForUser(userId);
        userAccountRepository.deleteUser(userId);
    }

}
