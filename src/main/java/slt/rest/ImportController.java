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
import slt.mapper.MyModelMapper;
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
    private MyModelMapper myModelMapper;

    private ImportService importService;

    @PostMapping
    public ResponseEntity<Void> setAll(@RequestBody final Export export) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        log.debug("export = {}", export);
        final var allFoodDtos = export.getAllFood();
        for (FoodDto foodDto : allFoodDtos) {
            final var food = myModelMapper.getConfiguredMapper().map(foodDto, Food.class);
            food.setId(null); // force a new entry
            final var savedFood = foodRepository.saveFood(userInfo.getUserId(), food);
            final var portionDtos = foodDto.getPortions();
            for (final var portionDto : portionDtos) {
                final var portion = importService.mapPortionDtoToPortion(portionDto);
                portionRepository.savePortion(savedFood.getId(), portion);
            }
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
                .map(s -> myModelMapper.getConfiguredMapper().map(s, Setting.class))
                .forEach(settingDomain -> {
                    settingDomain.setUserId(userInfo.getUserId());
                    settingDomain.setId(null); // force add new entry
                    settingsRepo.putSetting(settingDomain);
                });

        for (final var settingDto : settingDtos) {
            final var setting = myModelMapper.getConfiguredMapper().map(settingDto, Setting.class);
            setting.setId(null);
            setting.setUserId(userInfo.getUserId());
            settingsRepo.putSetting(setting);
        }

        final var allWeights = export.getAllWeights();
        allWeights.stream()
                .map(w -> myModelMapper.getConfiguredMapper().map(w, Weight.class))
                .forEach(weightDomain -> {
                    weightDomain.setId(null);// force add new entry
                    weightDomain.setUserId(userInfo.getUserId());
                    weightRepository.saveWeight(weightDomain);
                });

        final var allActivities = export.getAllActivities();
        allActivities.stream().map(a -> myModelMapper.getConfiguredMapper().map(a, Activity.class))
                .forEach(
                        activityDomain -> {
                            activityDomain.setId(null); // force add new entry
                            activityDomain.setUserId(userInfo.getUserId());
                            activityRepository.saveActivity(activityDomain);
                        });

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
