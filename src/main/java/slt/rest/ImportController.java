package slt.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import slt.database.*;
import slt.database.entities.*;
import slt.dto.*;
import slt.mapper.*;
import slt.security.ThreadLocalHolder;
import slt.service.ImportService;

@Slf4j
@RestController
@RequestMapping("/import")
@AllArgsConstructor
public class ImportController {

    private FoodRepository foodRepository;
    private PortionRepository portionRepository;
    private LogEntryRepository logEntryRepository;
    private SettingsRepository settingsRepo;
    private ActivityRepository activityRepository;
    private WeightRepository weightRepository;

    private final FoodMapper foodMapper = FoodMapper.INSTANCE;
    private final ActivityMapper activityMapper = ActivityMapper.INSTANCE;
    private final WeightMapper weightMapper = WeightMapper.INSTANCE;
    private final SettingsMapper settingsMapper = SettingsMapper.INSTANCE;

    private ImportService importService;

    @PostMapping
    public ResponseEntity<Void> setAll(@RequestBody final Export export) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        log.debug("export = {}", export);
        final var allFoodDtos = export.getAllFood();
        for (final var foodDto : allFoodDtos) {
            foodDto.setId(null); // force new entry
            foodDto.getPortions().forEach(portionDto -> portionDto.setId(null));
            final var food = foodMapper.map(foodDto);
            foodRepository.saveFood(food);
        }

        // To get the food_id's
        final var allFoodDB = foodRepository.getAllFood(userInfo.getUserId());
        final var entryDtos = export.getAllLogEntries();
        for (final var entryDto : entryDtos) {
            final var logEntry = importService.mapLogEntryDtoToLogEntry(entryDto);

            final var foodDB = importService.getFoodFromListByName(entryDto.getFood().getName(), allFoodDB);
            logEntry.setFoodId(foodDB.getId());
            if (entryDto.getPortion() != null) {
                final var portionDB = portionRepository.getPortion(foodDB.getId(), entryDto.getPortion().getDescription());
                logEntry.setPortionId(portionDB.getId());
            }
            logEntryRepository.saveLogEntry(userInfo.getUserId(), logEntry);
        }

        final var settingDtos = export.getAllSettingDtos();
        settingDtos.stream()
                .map(s -> settingsMapper.map(s, userInfo.getUserId()))
                .forEach(settingDomain -> {
                    settingDomain.setId(null); // force add new entry
                    settingsRepo.putSetting(settingDomain);
                });

        for (final var settingDto : settingDtos) {
            final var setting = settingsMapper.map(settingDto, userInfo.getUserId());
            setting.setId(null);
            settingsRepo.putSetting(setting);
        }

        final var allWeights = export.getAllWeights();
        allWeights.stream()
                .map(w -> weightMapper.map(w, userInfo.getUserId()))
                .forEach(weightDomain -> {
                    weightDomain.setId(null);// force add new entry
                    weightRepository.saveWeight(weightDomain);
                });

        final var allActivities = export.getAllActivities();
        allActivities.stream().map(a -> activityMapper.map(a, userInfo.getUserId()))
                .forEach(
                        activityDomain -> {
                            activityDomain.setId(null); // force add new entry
                            activityRepository.saveActivity(activityDomain);
                        });

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
