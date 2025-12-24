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

import java.util.List;

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
            final var food = myModelMapper.getConfiguredMapper().map(foodDto,Food.class);
            food.setId(null); // force a new entry
            final var savedFood = foodRepository.saveFood(userInfo.getUserId(), food);
            final var portionDtos = foodDto.getPortions();
            for (PortionDto portionDto : portionDtos) {
                Portion portion = importService.mapPortionDtoToPortion(portionDto);
                portionRepository.savePortion(savedFood.getId(), portion);
            }
        }

        // To get the food_id's
        List<Food> allFoodDB = foodRepository.getAllFood(userInfo.getUserId());

        List<EntryDto> entryDtos = export.getAllLogEntries();
        for (EntryDto entryDto : entryDtos) {
            LogEntry logEntry = importService.mapLogEntryDtoToLogEntry(entryDto);

            Food foodDB = importService.getFoodFromListByName(entryDto.getFood().getName(), allFoodDB);
            logEntry.setFoodId(foodDB.getId());
            if (entryDto.getPortion() != null) {
                Portion portionDB = portionRepository.getPortion(foodDB.getId(), entryDto.getPortion().getDescription());
                logEntry.setPortionId(portionDB.getId());
            }
            logEntryRepository.saveLogEntry(userInfo.getUserId(), logEntry);
        }

        List<SettingDto> settingDtos = export.getAllSettingDtos();
        settingDtos.stream()
                .map(s -> myModelMapper.getConfiguredMapper().map(s, Setting.class))
                .forEach(settingDomain -> {
                    settingDomain.setUserId(userInfo.getUserId());
                    settingDomain.setId(null); // force add new entry
                    settingsRepo.putSetting(settingDomain);
                });

        for (SettingDto settingDto : settingDtos) {
            Setting setting = myModelMapper.getConfiguredMapper().map(settingDto, Setting.class);
            setting.setId(null);
            setting.setUserId(userInfo.getUserId());
            settingsRepo.putSetting(setting);
        }

        List<WeightDto> allWeights = export.getAllWeights();
        allWeights.stream()
                .map(w -> myModelMapper.getConfiguredMapper().map(w, Weight.class))
                .forEach(weightDomain -> {
                    weightDomain.setId(null);
                    weightDomain.setUserId(userInfo.getUserId());// force add new entry
                    weightRepository.saveWeight( weightDomain);
                });

        List<LogActivityDto> allActivities = export.getAllActivities();
        allActivities.stream().map(a -> myModelMapper.getConfiguredMapper().map(a, LogActivity.class))
                .forEach(
                        activityDomain -> {
                            activityDomain.setId(null); // force add new entry
                            activityRepository.saveActivity(userInfo.getUserId(), activityDomain);
                        });

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
