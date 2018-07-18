package csl.rest;

import csl.database.FoodRepository;
import csl.database.LogEntryRepository;
import csl.database.PortionRepository;
import csl.database.SettingsRepository;
import csl.database.model.Food;
import csl.dto.*;
import csl.enums.MeasurementUnit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/export")
public class ExportService {

    private final static FoodRepository foodRepository = new FoodRepository();
    private final static PortionRepository portionRepository = new PortionRepository();
    private LogEntryRepository logEntryRepository = new LogEntryRepository();
    private SettingsRepository settingsRepo = new SettingsRepository();



    @ApiOperation(value = "Retrieve all stored information")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAll() {

        Export export = new Export();
        List<Food> allFood = foodRepository.getAllFood();
        List<csl.dto.Food> allFoodDtos = new ArrayList<>();
        for (Food food : allFood) {
            allFoodDtos.add(createFoodDto(food,true));
        }
        export.setAllFood(allFoodDtos);

        List<csl.database.model.LogEntry> allLogEntries = logEntryRepository.getAllLogEntries();

        List<LogEntry> allDtos = new ArrayList<>();
        for (csl.database.model.LogEntry logEntry : allLogEntries) {

            LogEntry dto = new LogEntry();
            Food food = foodRepository.getFoodById(logEntry.getFoodId());
            dto.setId(logEntry.getId());
            csl.dto.Food foodDto = FoodService.mapFoodToFoodDto(food);
            dto.setFood(foodDto);

            csl.database.model.Portion portion = null;
            if (logEntry.getPortionId()!= null && logEntry.getPortionId()!= 0){
                portion = portionRepository.getPortion(logEntry.getPortionId());
                csl.dto.Portion portionDto = new csl.dto.Portion();
                portionDto.setId(portion.getId());
                portionDto.setGrams(portion.getGrams());
                portionDto.setDescription(portion.getDescription());
                portionDto.setUnitMultiplier(portion.getUnitMultiplier());
                Macro calculatedMacros = FoodService.calculateMacro(food, portion);
                portionDto.setMacros(calculatedMacros);
                dto.setPortion(portionDto);
            }
            Double multiplier = logEntry.getMultiplier();
            dto.setMultiplier(multiplier);
            dto.setDay(logEntry.getDay());
            dto.setMeal(logEntry.getMeal());

            Macro macrosCalculated = new Macro();
            if (portion!= null){
                macrosCalculated = dto.getPortion().getMacros().clone();
                macrosCalculated.multiply(multiplier);

            } else {
                macrosCalculated.setCarbs(multiplier * food.getCarbs());
                macrosCalculated.setFat(multiplier * food.getFat());
                macrosCalculated.setProtein(multiplier * food.getProtein());
            }
            dto.setMacrosCalculated(macrosCalculated);

            allDtos.add(dto);
        }

        export.setAllLogEntries(allDtos);

        List<Setting> settings = settingsRepo.getAllSettings();
        export.setAllSettings(settings);



        return ResponseEntity.ok(export);
    }

    public csl.dto.Food createFoodDto(Food food, boolean withPortions) {
        csl.dto.Food foodDto = mapFoodToFoodDto(food);

        if (withPortions) {
            List<csl.database.model.Portion> foodPortions = portionRepository.getPortions(food.getId());
            for (csl.database.model.Portion portion : foodPortions) {
                Portion currDto = new Portion();
                currDto.setDescription(portion.getDescription());
                currDto.setGrams(portion.getGrams());
                currDto.setUnitMultiplier(portion.getUnitMultiplier());
                currDto.setId(portion.getId());

                foodDto.addPortion(currDto);
            }
        }
        return foodDto;
    }


    public static  csl.dto.Food mapFoodToFoodDto(Food food) {
        csl.dto.Food foodDto = new csl.dto.Food();
        foodDto.setName(food.getName());
        foodDto.setId(food.getId());
        foodDto.setMeasurementUnit(food.getMeasurementUnit());
        foodDto.setUnitGrams(food.getUnitGrams());
        foodDto.setUnitName(food.getUnitName());
        foodDto.setProtein(food.getProtein());
        foodDto.setCarbs(food.getCarbs());
        foodDto.setFat(food.getFat());
        return foodDto;
    }


}
