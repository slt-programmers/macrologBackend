package slt.rest;

import io.swagger.annotations.ApiOperation;
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
import slt.database.model.*;
import slt.dto.*;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;

import java.sql.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

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

    @ApiOperation(value = "Import exported json")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity setAll(@RequestBody Export export) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        log.debug("export = " + export);

        List<Setting> settings = export.getAllSettings();
        for (Setting setting : settings) {
            settingsRepo.putSetting(userInfo.getUserId(), setting.getName(), setting.getValue(), setting.getDay());
        }

        List<FoodDto> allFoodDto = export.getAllFood();
        for (FoodDto foodDto : allFoodDto) {
            Food food = mapFoodDtoToFood(foodDto);
            foodRepository.insertFood(userInfo.getUserId(), food);

            // We hebben de database ID nodig, dus opnieuw ophalen:
            Food foodDB = foodRepository.getFood(userInfo.getUserId(), food.getName());
            List<PortionDto> portionDtos = foodDto.getPortions();

            for (PortionDto portionDto : portionDtos) {
                Portion portion = mapPortionDtoToPortion(portionDto);
                portionRepository.addPortion(foodDB.getId(), portion);
            }
        }

        // To get the food_id's
        List<Food> allFoodDB = foodRepository.getAllFood(userInfo.getUserId());

        List<LogEntryDto> logEntryDtos = export.getAllLogEntries();
        for (LogEntryDto logEntryDto : logEntryDtos) {
            LogEntry logEntry = mapLogEntryDtoToLogEntry(logEntryDto);

            Food foodDB = getFoodFromListByName(logEntryDto.getFood().getName(), allFoodDB);
            logEntry.setFoodId(foodDB.getId());
            if (logEntryDto.getPortion() != null) {
                Portion portionDB = portionRepository.getPortion(foodDB.getId(), logEntryDto.getPortion().getDescription());
                logEntry.setPortionId(portionDB.getId());
            }
            logEntryRepository.insertLogEntry(userInfo.getUserId(), logEntry);
        }

        List<WeightDto> allWeights = export.getAllWeights();
        allWeights.stream().map(this::mapWeightToDomain)
                .forEach(weightDomain -> weightRepository.insertWeight(userInfo.getUserId(), weightDomain));

        List<LogActivityDto> allActivities = export.getAllActivities();
        allActivities.stream().map(this::mapActivityDtoToDomain)
                .forEach(activityDomain -> activityRepository.insertActivity(userInfo.getUserId(), activityDomain));

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private Food getFoodFromListByName(String foodName, List<Food> foodList) {
        Food foundFood;
        List<Food> matches = foodList.stream()
                .filter(food -> food.getName().equals(foodName))
                .collect(toList());
        if (matches.size() == 1) {
            foundFood = matches.get(0);
        } else {
            log.error("Multiple Food with name " + foodName + " found");
            log.error("Selecting first from list");
            foundFood = matches.get(0);
        }
        return foundFood;
    }

    private static LogEntry mapLogEntryDtoToLogEntry(LogEntryDto logEntryDto) {
        LogEntry logEntry = new LogEntry();
        logEntry.setId(null);
        java.sql.Date newDate = new java.sql.Date(logEntryDto.getDay().getTime());
        logEntry.setDay(newDate);
        logEntry.setFoodId(logEntryDto.getFood().getId());
        logEntry.setMeal(logEntryDto.getMeal());
        logEntry.setMultiplier(logEntryDto.getMultiplier());
        if (logEntryDto.getPortion() != null) {
            logEntry.setPortionId(logEntryDto.getPortion().getId());
        }
        return logEntry;
    }

    private static Portion mapPortionDtoToPortion(PortionDto portionDto) {
        Portion portion = new Portion();
        portion.setId(null);
        portion.setGrams(portionDto.getGrams());
        portion.setDescription(portionDto.getDescription());
        return portion;
    }

    private static Food mapFoodDtoToFood(FoodDto foodDto) {
        Food food = new Food();
        food.setName(foodDto.getName());
        food.setId(null);
        food.setProtein(foodDto.getProtein());
        food.setCarbs(foodDto.getCarbs());
        food.setFat(foodDto.getFat());
        return food;
    }

    private Weight mapWeightToDomain(WeightDto weightEntry) {
        Weight entry = new Weight();
        entry.setDay(Date.valueOf(weightEntry.getDay()));
        entry.setId(null);
        entry.setWeight(weightEntry.getWeight());
        entry.setRemark(weightEntry.getRemark());
        return entry;
    }

    private LogActivity mapActivityDtoToDomain(LogActivityDto logEntry) {
        LogActivity entry = new LogActivity();
        entry.setName(logEntry.getName());
        entry.setCalories(logEntry.getCalories());
        entry.setDay(new Date(logEntry.getDay().getTime()));
        entry.setId(logEntry.getId());
        return entry;
    }
}
