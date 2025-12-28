package slt.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import slt.database.*;
import slt.database.entities.*;
import slt.dto.*;
import slt.mapper.MyModelMapper;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.util.MacroUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/export")
@AllArgsConstructor
public class ExportService {

    private MyModelMapper myModelMapper;
    private FoodRepository foodRepository;
    private SettingsRepository settingsRepo;
    private PortionRepository portionRepository;
    private LogEntryRepository logEntryRepository;
    private ActivityRepository activityRepository;
    private WeightRepository weightRepository;

    @GetMapping
    public ResponseEntity<Export> getAll() {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        Export export = new Export();
        List<Food> allFood = foodRepository.getAllFood(userInfo.getUserId());
        log.info("Export: allFood size = " + allFood.size());

        List<FoodDto> allFoodDtos = new ArrayList<>();
        for (Food food : allFood) {
            allFoodDtos.add(createFoodDto(food));
        }
        log.info("Export: allFoodDtos size = " + allFoodDtos.size());
        export.setAllFood(allFoodDtos);

        List<LogEntry> allLogEntries = logEntryRepository.getAllLogEntries(userInfo.getUserId());

        List<EntryDto> allDtos = new ArrayList<>();
        for (LogEntry logEntry : allLogEntries) {

            EntryDto entryDto = new EntryDto();
            entryDto.setId(logEntry.getId());
            log.info("Export: logEntryDto ID " + logEntry.getFoodId());

            FoodDto foodDto = allFoodDtos.stream().filter(f -> {
                log.info("Export: foodDto ID " + f.getId());
                return f.getId().equals(logEntry.getFoodId());
            }).findFirst().orElseGet(() -> {
                        Food foodById = foodRepository.getFoodById(userInfo.getUserId(), logEntry.getFoodId());
                        return myModelMapper.getConfiguredMapper().map(foodById, FoodDto.class);
                    }
            );

            entryDto.setFood(foodDto);

            PortionDto portionDto = null;
            if (logEntry.getPortionId() != null && logEntry.getPortionId() != 0) {
                portionDto = foodDto.getPortions().stream().filter(p -> p.getId().equals(logEntry.getPortionId())).findFirst()
                        .orElse(null);
                if (portionDto != null) {
                    MacroDto calculatedMacros = MacroUtils.calculateMacro(foodDto, portionDto);
                    portionDto.setMacros(calculatedMacros);
                }
                entryDto.setPortion(portionDto);
            }
            Double multiplier = logEntry.getMultiplier();
            entryDto.setMultiplier(multiplier);
            entryDto.setDay(logEntry.getDay());
            entryDto.setMeal(Meal.valueOf(logEntry.getMeal()));

            MacroDto macrosCalculated = new MacroDto();
            if (portionDto != null) {
                macrosCalculated = entryDto.getPortion().getMacros().createCopy();
                macrosCalculated = MacroUtils.multiply(macrosCalculated, multiplier);
            } else {
                macrosCalculated.setCarbs(multiplier * foodDto.getCarbs());
                macrosCalculated.setFat(multiplier * foodDto.getFat());
                macrosCalculated.setProtein(multiplier * foodDto.getProtein());
            }
            entryDto.setMacrosCalculated(macrosCalculated);

            allDtos.add(entryDto);
        }

        export.setAllLogEntries(allDtos);

        List<Setting> settings = settingsRepo.getAllSettings(userInfo.getUserId());
        List<SettingDto> collectedSettingsDto = settings.stream()
                .map(s -> myModelMapper.getConfiguredMapper().map(s, SettingDto.class))
                .collect(Collectors.toList());
        export.setAllSettingDtos(collectedSettingsDto);

        List<Activity> activities = activityRepository.getAllActivities(userInfo.getUserId());
        List<ActivityDto> collectedActivityDtos = activities.stream()
                .map(a -> myModelMapper.getConfiguredMapper().map(a, ActivityDto.class))
                .collect(Collectors.toList());
        export.setAllActivities(collectedActivityDtos);

        List<Weight> allWeightEntries = weightRepository.getAllWeightEntries(userInfo.getUserId());
        List<WeightDto> collectedWeightDtos = allWeightEntries.stream()
                .map(w -> myModelMapper.getConfiguredMapper().map(w, WeightDto.class))
                .collect(Collectors.toList());
        export.setAllWeights(collectedWeightDtos);

        return ResponseEntity.ok(export);
    }

    private FoodDto createFoodDto(final Food food) {
        final var foodDto = myModelMapper.getConfiguredMapper().map(food, FoodDto.class);
        final var foodPortions = portionRepository.getPortions(food.getId());
        for (final var portion : foodPortions) {
            final var currDto = myModelMapper.getConfiguredMapper().map(portion, PortionDto.class);
            foodDto.addPortion(currDto);
        }
        return foodDto;
    }
}
