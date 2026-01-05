package slt.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import slt.dto.Export;

@Slf4j
@Service
@AllArgsConstructor
public class ExportService {

    private FoodService foodService;
    private SettingsService settingsService;
    private EntryService entryService;
    private ActivityService activityService;
    private WeightService weightService;
    private DishService dishService;
    private MealplanService mealplanService;

    public Export exportAllForUser(final Long userId) {
        log.info("Exporting all data for user [{}]", userId);
        return Export.builder()
                .allFood(foodService.getAllFood(userId))
                .allLogEntries(entryService.getAllEntries(userId))
                .allSettingDtos(settingsService.getAllUserSettings(userId))
                .allActivities(activityService.getAllActivities(userId))
                .allWeights(weightService.getAllWeights(userId))
                .allDishes(dishService.getAllDishes(userId))
                .allMealplans(mealplanService.getAllMealplans(userId))
                .build();
    }
}
