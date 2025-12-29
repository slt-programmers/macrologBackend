package slt.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import slt.database.*;
import slt.dto.Export;
import slt.mapper.*;

@Slf4j
@Service
@AllArgsConstructor
public class ImportService {

    private FoodRepository foodRepository;
    private EntryRepository entryRepository;
    private SettingsRepository settingsRepository;
    private ActivityRepository activityRepository;
    private WeightRepository weightRepository;

    private final FoodMapper foodMapper = FoodMapper.INSTANCE;
    private final ActivityMapper activityMapper = ActivityMapper.INSTANCE;
    private final WeightMapper weightMapper = WeightMapper.INSTANCE;
    private final SettingsMapper settingsMapper = SettingsMapper.INSTANCE;
    private final EntryMapper entryMapper = EntryMapper.INSTANCE;

    public void importAllForUser(final Long userId, final Export export) {
        log.debug("Export = {}", export);
        final var allFoodDtos = export.getAllFood();
        for (final var foodDto : allFoodDtos) {
            foodDto.setId(null); // force new entry
            foodDto.getPortions().forEach(portionDto -> portionDto.setId(null));
            final var food = foodMapper.map(foodDto, userId);
            foodRepository.saveFood(food);
        }

        final var entryDtos = export.getAllLogEntries();
        for (final var entryDto : entryDtos) {
            final var entry = entryMapper.map(entryDto, userId);
            entry.setId(null); // force new entry
            entryRepository.saveEntry(entry);
        }

        final var settingDtos = export.getAllSettingDtos();
        settingDtos.stream()
                .map(s -> settingsMapper.map(s, userId))
                .forEach(settingDomain -> {
                    settingDomain.setId(null); // force add new entry
                    settingsRepository.putSetting(settingDomain);
                });

        for (final var settingDto : settingDtos) {
            final var setting = settingsMapper.map(settingDto, userId);
            setting.setId(null);
            settingsRepository.putSetting(setting);
        }

        final var allWeights = export.getAllWeights();
        allWeights.stream()
                .map(w -> weightMapper.map(w, userId))
                .forEach(weightDomain -> {
                    weightDomain.setId(null);// force add new entry
                    weightRepository.saveWeight(weightDomain);
                });

        final var allActivities = export.getAllActivities();
        allActivities.stream().map(a -> activityMapper.map(a, userId))
                .forEach(
                        activityDomain -> {
                            activityDomain.setId(null); // force add new entry
                            activityRepository.saveActivity(activityDomain);
                        });
    }
}
