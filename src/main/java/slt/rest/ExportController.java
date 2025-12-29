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
import slt.security.ThreadLocalHolder;
import slt.service.*;

@Slf4j
@RestController
@RequestMapping("/export")
@AllArgsConstructor
public class ExportController {

    private FoodService foodService;
    private SettingsService settingsService;
    private EntryService entryService;
    private ActivityService activityService;
    private WeightService weightService;

    @GetMapping
    public ResponseEntity<Export> getAll() {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        log.info("Exporting all data for user [{}]", userInfo.getUserId());
        final var export = new Export();

        final var allFood = foodService.getAllFood(userInfo.getUserId());
        export.setAllFood(allFood);
        log.info("Food count: {}", allFood.size());

        final var allEntries = entryService.getAllEntries(userInfo.getUserId());
//        final var allLogEntries = entryRepository.getAllEntries(userInfo.getUserId());
//        List<EntryDto> allDtos = new ArrayList<>();
//        for (Entry entry : allLogEntries) {
//            EntryDto entryDto = new EntryDto();
//            entryDto.setId(entry.getId());
//            log.info("Export: logEntryDto ID " + entry.getFood().getId());
//
//            FoodDto foodDto = allFood.stream().filter(f -> {
//                log.info("Export: foodDto ID " + f.getId());
//                return f.getId().equals(entry.getFood().getId());
//            }).findFirst().orElseGet(() -> {
//                        final var optionalFood = foodRepository.getFoodById(userInfo.getUserId(), entry.getFood().getId());
//                        assert optionalFood.isPresent();
//                        return myModelMapper.getConfiguredMapper().map(optionalFood, FoodDto.class);
//                    }
//            );
//
//            entryDto.setFood(foodDto);
//
//            PortionDto portionDto = null;
//            if (entry.getPortion().getId() != null && entry.getPortion().getId() != 0) {
//                portionDto = foodDto.getPortions().stream().filter(p -> p.getId().equals(entry.getPortion().getId())).findFirst()
//                        .orElse(null);
//                if (portionDto != null) {
//                    MacroDto calculatedMacros = MacroUtils.calculateMacro(foodDto, portionDto);
//                    portionDto.setMacros(calculatedMacros);
//                }
//                entryDto.setPortion(portionDto);
//            }
//            Double multiplier = entry.getMultiplier();
//            entryDto.setMultiplier(multiplier);
//            entryDto.setDay(entry.getDay());
//            entryDto.setMeal(Meal.valueOf(entry.getMeal()));
//
//            MacroDto macrosCalculated = new MacroDto();
//            if (portionDto != null) {
//                macrosCalculated = entryDto.getPortion().getMacros().createCopy();
//                macrosCalculated = MacroUtils.multiply(macrosCalculated, multiplier);
//            } else {
//                macrosCalculated.setCarbs(multiplier * foodDto.getCarbs());
//                macrosCalculated.setFat(multiplier * foodDto.getFat());
//                macrosCalculated.setProtein(multiplier * foodDto.getProtein());
//            }
//            entryDto.setMacrosCalculated(macrosCalculated);
//
//            allDtos.add(entryDto);
//        }
        export.setAllLogEntries(allEntries);

        final var settings = settingsService.getAllUserSettings(userInfo.getUserId());
        export.setAllSettingDtos(settings);

        final var activities = activityService.getAllActivities(userInfo.getUserId());
        export.setAllActivities(activities);

        final var allWeights = weightService.getAllWeights(userInfo.getUserId());
        export.setAllWeights(allWeights);

        return ResponseEntity.ok(export);
    }

}
