package csl.rest;

import csl.database.FoodRepository;
import csl.database.LogEntryRepository;
import csl.database.PortionRepository;
import csl.database.SettingsRepository;
import csl.database.model.*;
import csl.dto.*;
import csl.dto.LogEntryDto;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/import")
public class ImportService {

    private final static FoodRepository foodRepository = new FoodRepository();
    private final static PortionRepository portionRepository = new PortionRepository();
    private LogEntryRepository logEntryRepository = new LogEntryRepository();
    private SettingsRepository settingsRepo = new SettingsRepository();

    @ApiOperation(value = "Import exported json")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity setAll(@RequestBody Export export) throws URISyntaxException {
        System.out.println(export);

        List<Setting> settings = export.getAllSettings();
        for (Setting setting : settings) {
            settingsRepo.insertSetting(setting.getName(), setting.getValue());
        }

        List<FoodDto> allFoodDto = export.getAllFood();
        for (FoodDto foodDto : allFoodDto) {
            Food food = mapFoodDtoToFood(foodDto);
            foodRepository.insertFood(food);

            // We hebben de database ID nodig, dus opnieuw ophalen:
            Food foodDB = foodRepository.getFood(food.getName());
            List<PortionDto> portionDtos = foodDto.getPortions();

            for (PortionDto portionDto: portionDtos) {
                Portion portion = mapPortionDtoToPortion(portionDto);
                portionRepository.addPortion(foodDB.getId(), portion);
            }
        }

        List<LogEntryDto> logEntryDtos = export.getAllLogEntries();
        for (LogEntryDto logEntryDto: logEntryDtos) {
            LogEntry logEntry = mapLogEntryDtoToLogEntry(logEntryDto);
            // De database IDs komen waarschijnlijk niet overeen, dus haal de correcte database ids op:
            Food foodDB = foodRepository.getFood(logEntryDto.getFood().getName());
            Portion portionDB = portionRepository.getPortion(foodDB.getId(), logEntryDto.getPortion().getDescription());
            logEntry.setFoodId(foodDB.getId());
            logEntry.setPortionId(portionDB.getId());
            logEntryRepository.insertLogEntry(logEntry);
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    public static LogEntry mapLogEntryDtoToLogEntry(LogEntryDto logEntryDto) {
        LogEntry logEntry = new LogEntry();
        logEntry.setId(null);
        java.sql.Date newDate = new java.sql.Date(logEntryDto.getDay().getTime());
        logEntry.setDay(newDate);
        logEntry.setFoodId(logEntryDto.getFood().getId());
        logEntry.setMeal(logEntryDto.getMeal());
        logEntry.setMultiplier(logEntryDto.getMultiplier());
        logEntry.setPortionId(logEntryDto.getPortion().getId());
        return logEntry;
    }

    public static Portion mapPortionDtoToPortion(PortionDto portionDto) {
        Portion portion = new Portion();
        portion.setId(null);
        portion.setGrams(portionDto.getGrams());
        portion.setDescription(portionDto.getDescription());
        portion.setUnitMultiplier(portionDto.getUnitMultiplier());
        return portion;
    }

    public static Food mapFoodDtoToFood(FoodDto foodDto) {
        Food food = new Food();
        food.setName(foodDto.getName());
        food.setId(null);
        food.setMeasurementUnit(foodDto.getMeasurementUnit());
        food.setUnitGrams(foodDto.getUnitGrams());
        food.setUnitName(foodDto.getUnitName());
        food.setProtein(foodDto.getProtein());
        food.setCarbs(foodDto.getCarbs());
        food.setFat(foodDto.getFat());
        return food;
    }

}
