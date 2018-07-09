package csl.rest;

import csl.database.FoodRepository;
import csl.database.LogEntryRepository;
import csl.database.PortionRepository;
import csl.database.model.Food;
import csl.database.model.Portion;
import csl.dto.AddLogEntryRequest;
import csl.dto.LogEntry;
import csl.dto.Macro;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

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
                macrosCalculated.setProteins(multiplier * food.getProtein());
            }
            dto.setMacrosCalculated(macrosCalculated);

            allDtos.add(dto);
        }
        return ResponseEntity.ok(allDtos);


    }

    @ApiOperation(value = "Store new logentry")
    @RequestMapping(value = "",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity storeLogEntry(@RequestBody AddLogEntryRequest logEntry) {

        csl.database.model.LogEntry entry = new csl.database.model.LogEntry();
        entry.setPortionId(logEntry.getPortionId());
        entry.setFoodId(logEntry.getFoodId());
        entry.setMultiplier(logEntry.getMultiplier());
        entry.setDay(new Date(logEntry.getDay().getTime()));
        entry.setMeal(logEntry.getMeal());
        logEntryRepository.insertLogEntry(entry);

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
