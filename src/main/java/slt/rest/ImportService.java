package slt.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import slt.security.UserInfo;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/import")
public class ImportService {

    @Autowired
    private FoodRepository foodRepository;
    @Autowired
    private PortionRepository portionRepository;
    @Autowired
    private LogEntryRepository logEntryRepository;
    @Autowired
    private SettingsRepository settingsRepo;
    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private WeightRepository weightRepository;
    @Autowired
    private MyModelMapper myModelMapper;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> setAll(@RequestBody final Export export) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        log.debug("export = " + export);
        List<FoodDto> allFoodDto = export.getAllFood();
        for (FoodDto foodDto : allFoodDto) {
            Food food = myModelMapper.getConfiguredMapper().map(foodDto,Food.class);
            food.setId(null);// force a new entry
            Food foodDB = foodRepository.saveFood(userInfo.getUserId(), food);

            List<PortionDto> portionDtos = foodDto.getPortions();

            for (PortionDto portionDto : portionDtos) {
                Portion portion = mapPortionDtoToPortion(portionDto);
                portionRepository.savePortion(foodDB.getId(), portion);
            }
        }

        // To get the food_id's
        List<Food> allFoodDB = foodRepository.getAllFood(userInfo.getUserId());

        List<EntryDto> entryDtos = export.getAllLogEntries();
        for (EntryDto entryDto : entryDtos) {
            LogEntry logEntry = mapLogEntryDtoToLogEntry(entryDto);

            Food foodDB = getFoodFromListByName(entryDto.getFood().getName(), allFoodDB);
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
                    settingDomain.setId(null); // force add new entry
                    settingsRepo.putSetting(userInfo.getUserId(),settingDomain);
                });

        for (SettingDto settingDto : settingDtos) {
            Setting setting = myModelMapper.getConfiguredMapper().map(settingDto, Setting.class);
            setting.setId(null);
            settingsRepo.putSetting(userInfo.getUserId(), setting);
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

    private Food getFoodFromListByName(final String foodName, final List<Food> foodList) {
        Food foundFood;
        List<Food> matches = foodList.stream()
                .filter(food -> food.getName().equals(foodName))
                .toList();
        if (matches.size() == 1) {
            foundFood = matches.getFirst();
        } else {
            log.error("Multiple Food with name " + foodName + " found");
            log.error("Selecting first from list");
            foundFood = matches.getFirst();
        }
        return foundFood;
    }

    private static LogEntry mapLogEntryDtoToLogEntry(final EntryDto entryDto) {
        LogEntry logEntry = new LogEntry();
        logEntry.setId(null);
        java.sql.Date newDate = new java.sql.Date(entryDto.getDay().getTime());
        logEntry.setDay(newDate);
        logEntry.setFoodId(entryDto.getFood().getId());
        logEntry.setMeal(entryDto.getMeal().toString());
        logEntry.setMultiplier(entryDto.getMultiplier());
        if (entryDto.getPortion() != null) {
            logEntry.setPortionId(entryDto.getPortion().getId());
        }
        return logEntry;
    }

    private static Portion mapPortionDtoToPortion(final PortionDto portionDto) {
        Portion portion = new Portion();
        portion.setId(null);
        portion.setGrams(portionDto.getGrams());
        portion.setDescription(portionDto.getDescription());
        return portion;
    }


}
