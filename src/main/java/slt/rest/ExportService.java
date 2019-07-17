package slt.rest;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import slt.database.*;
import slt.database.entities.*;
import slt.dto.*;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/export")
public class ExportService {

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

    @ApiOperation(value = "Retrieve all stored information")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAll() {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        Export export = new Export();
        List<Food> allFood = foodRepository.getAllFood(userInfo.getUserId());
        log.info("Export: allFood size = " + allFood.size());

        List<FoodDto> allFoodDtos = new ArrayList<>();
        for (Food food : allFood) {
            allFoodDtos.add(createFoodDto(food, true));
        }
        log.info("Export: allFoodDtos size = " + allFoodDtos.size());
        export.setAllFood(allFoodDtos);

        List<LogEntry> allLogEntries = logEntryRepository.getAllLogEntries(userInfo.getUserId());

        List<LogEntryDto> allDtos = new ArrayList<>();
        for (LogEntry logEntry : allLogEntries) {

            LogEntryDto logEntryDto = new LogEntryDto();
            logEntryDto.setId(logEntry.getId());
            log.info("Export: logEntryDto ID " + logEntry.getFoodId());

            FoodDto foodDto = allFoodDtos.stream().filter(f -> {
                log.info("Export: foodDto ID " + f.getId());
                return f.getId().equals(logEntry.getFoodId());
            }).findFirst().orElseGet(() ->
                    FoodService.mapFoodToFoodDto(foodRepository.getFoodById(userInfo.getUserId(), logEntry.getFoodId())));
            logEntryDto.setFood(foodDto);

            PortionDto portionDto = null;
            if (logEntry.getPortionId() != null && logEntry.getPortionId() != 0) {
                portionDto = foodDto.getPortions().stream().filter(p -> p.getId().equals(logEntry.getPortionId())).findFirst()
                        .orElse(null);
                if (portionDto != null) {
                    Macro calculatedMacros = FoodService.calculateMacro(foodDto, portionDto);
                    portionDto.setMacros(calculatedMacros);
                }
                logEntryDto.setPortion(portionDto);
            }
            Double multiplier = logEntry.getMultiplier();
            logEntryDto.setMultiplier(multiplier);
            logEntryDto.setDay(logEntry.getDay());
            logEntryDto.setMeal(logEntry.getMeal());

            Macro macrosCalculated = new Macro();
            if (portionDto != null) {
                macrosCalculated = logEntryDto.getPortion().getMacros().createCopy();
                macrosCalculated.multiply(multiplier);

            } else {
                macrosCalculated.setCarbs(multiplier * foodDto.getCarbs());
                macrosCalculated.setFat(multiplier * foodDto.getFat());
                macrosCalculated.setProtein(multiplier * foodDto.getProtein());
            }
            logEntryDto.setMacrosCalculated(macrosCalculated);

            allDtos.add(logEntryDto);
        }

        export.setAllLogEntries(allDtos);

        List<Setting> settings = settingsRepo.getAllSettings(userInfo.getUserId());
        export.setAllSettingDtos(transformToOldSetting(settings));

        List<LogActivity> activities = activityRepository.getAllLogActivities(userInfo.getUserId());
        List<LogActivityDto> collectedActivityDtos = activities.stream().map(this::mapActivityToDto).collect(Collectors.toList());
        export.setAllActivities(collectedActivityDtos);

        List<Weight> allWeightEntries = weightRepository.getAllWeightEntries(userInfo.getUserId());
        List<WeightDto> collectedWeightDtos = allWeightEntries.stream().map(this::mapWeightToDto).collect(Collectors.toList());
        export.setAllWeights(collectedWeightDtos);

        return ResponseEntity.ok(export);
    }

    private FoodDto createFoodDto(Food food, boolean withPortions) {
        FoodDto foodDto = mapFoodToFoodDto(food);

        if (withPortions) {
            List<Portion> foodPortions = portionRepository.getPortions(food.getId());
            for (Portion portion : foodPortions) {

                PortionDto currDto = new PortionDto();
                currDto.setDescription(portion.getDescription());
                currDto.setGrams(portion.getGrams());
                currDto.setId(portion.getId());

                foodDto.addPortion(currDto);
            }
        }
        return foodDto;
    }

    private static FoodDto mapFoodToFoodDto(Food food) {
        FoodDto foodDto = new FoodDto();
        foodDto.setName(food.getName());
        foodDto.setId(food.getId());
        foodDto.setProtein(food.getProtein());
        foodDto.setCarbs(food.getCarbs());
        foodDto.setFat(food.getFat());
        return foodDto;
    }

    private LogActivityDto mapActivityToDto(LogActivity logEntry) {
        LogActivityDto dto = new LogActivityDto();
        dto.setCalories(logEntry.getCalories());
        dto.setName(logEntry.getName());
        dto.setId(logEntry.getId());
        dto.setDay(logEntry.getDay());
        return dto;
    }

    private WeightDto mapWeightToDto(Weight weightEntry) {
        WeightDto dto = new WeightDto();
        dto.setDay(weightEntry.getDay().toLocalDate());
        dto.setId(weightEntry.getId().longValue());
        dto.setWeight(weightEntry.getValue());
        dto.setRemark(weightEntry.getRemark());

        return dto;
    }
    private List<SettingDto> transformToOldSetting(List<slt.database.entities.Setting> newSetting) {
        return newSetting.stream().map(this::transformToOldSetting).collect(Collectors.toList());
    }

    private SettingDto transformToOldSetting(slt.database.entities.Setting newSetting) {
        return SettingDto.builder().name(newSetting.getName())
                .value(newSetting.getValue())
                .day(newSetting.getDay())
                .id(newSetting.getId().longValue())
                .build();
    }

}
