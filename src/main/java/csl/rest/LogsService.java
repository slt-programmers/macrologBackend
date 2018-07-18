package csl.rest;

import csl.database.FoodRepository;
import csl.database.LogEntryRepository;
import csl.database.PortionRepository;
import csl.database.model.Food;
import csl.database.model.Portion;
import csl.dto.StoreLogEntryRequest;
import csl.dto.LogEntry;
import csl.dto.Macro;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/logs")
@Api(value = "logs", description = "Operations pertaining to logentries in the macro logger applications")
public class LogsService {

    private FoodRepository foodRepository = new FoodRepository();
    private PortionRepository portionRepository = new PortionRepository();
    private LogEntryRepository logEntryRepository = new LogEntryRepository();


    @ApiOperation(value = "Retrieve all stored logentries")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllLogEntries() {

        List<csl.database.model.LogEntry> allLogEntries = logEntryRepository.getAllLogEntries();

        List<LogEntry> allDtos = new ArrayList<>();
        for (csl.database.model.LogEntry logEntry : allLogEntries) {

            LogEntry dto = new LogEntry();
            Food food = foodRepository.getFoodById(logEntry.getFoodId());
            dto.setId(logEntry.getId());
            csl.dto.Food foodDto = FoodService.mapFoodToFoodDto(food);
            dto.setFood(foodDto);

            Portion portion = null;
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
        return ResponseEntity.ok(allDtos);


    }

    @ApiOperation(value = "Retrieve all stored logentries")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/day/{date}",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getLogOfDay(@PathVariable("date") String dateLog) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        java.util.Date parsedDate;
        try {
             parsedDate = sdf.parse(dateLog);
        } catch (ParseException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        List<csl.database.model.LogEntry> allLogEntries = logEntryRepository.getAllLogEntries(parsedDate);

        List<LogEntry> allDtos = new ArrayList<>();
        for (csl.database.model.LogEntry logEntry : allLogEntries) {

            LogEntry dto = new LogEntry();
            Food food = foodRepository.getFoodById(logEntry.getFoodId());
            dto.setId(logEntry.getId());
            csl.dto.Food foodDto = FoodService.mapFoodToFoodDto(food);
            dto.setFood(foodDto);

            Portion portion = null;
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
        return ResponseEntity.ok(allDtos);


    }

    @ApiOperation(value = "Store logentry")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity storeLogEntry(@RequestBody StoreLogEntryRequest logEntry) {

        csl.database.model.LogEntry entry = new csl.database.model.LogEntry();
        entry.setPortionId(logEntry.getPortionId());
        entry.setFoodId(logEntry.getFoodId());
        entry.setMultiplier(logEntry.getMultiplier());
        entry.setDay(new Date(logEntry.getDay().getTime()));
        entry.setMeal(logEntry.getMeal());
        entry.setId(logEntry.getId());
        if (logEntry.getId() == null) {
            logEntryRepository.insertLogEntry(entry);
        } else {
            logEntryRepository.updateLogEntry(entry);
        }


        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @ApiOperation(value = "Delete logentry")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/{id}",
            method = DELETE,
            headers = {"Content-Type=application/json"})
    public ResponseEntity storeLogEntry(@PathVariable("id") Long logEntryId) {

        logEntryRepository.deleteLogEntry(logEntryId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
