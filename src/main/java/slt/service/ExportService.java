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
        final var export = new Export();

        final var allFood = foodService.getAllFood(userId);
        export.setAllFood(allFood);

        final var allEntries = entryService.getAllEntries(userId);
        export.setAllLogEntries(allEntries);

        final var settings = settingsService.getAllUserSettings(userId);
        export.setAllSettingDtos(settings);

        final var activities = activityService.getAllActivities(userId);
        export.setAllActivities(activities);

        final var allWeights = weightService.getAllWeights(userId);
        export.setAllWeights(allWeights);

        final var allDishes = dishService.getAllDishes(userId);
        export.setAllDishes(allDishes);

        final var allMealplans = mealplanService.getAllMealplans(userId);
        export.setAllMealplans(allMealplans);

        return export;
    }
}
