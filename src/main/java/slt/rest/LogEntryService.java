package slt.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.database.FoodRepository;
import slt.database.LogEntryRepository;
import slt.database.PortionRepository;
import slt.database.model.Food;
import slt.database.model.LogEntry;
import slt.database.model.Portion;
import slt.dto.*;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/logs")
@Api(value = "logs")
@Slf4j
public class LogEntryService {

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private PortionRepository portionRepository;

    @Autowired
    private LogEntryRepository logEntryRepository;

    @ApiOperation(value = "Retrieve all stored logentries for date")
    @GetMapping(path = "/day/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getLogEntriesForDay(@PathVariable("date") String date) {

        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        log.debug("Request for " + userInfo);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date parsedDate;
        try {
            parsedDate = sdf.parse(date);
        } catch (ParseException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        List<slt.database.model.LogEntry> allLogEntries = logEntryRepository.getAllLogEntries(userInfo.getUserId(), parsedDate);
        List<LogEntryDto> logEntryDtos = mapToDtos(userInfo, allLogEntries);

        return ResponseEntity.ok(logEntryDtos);
    }

    @ApiOperation(value = "Store logentries")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity storeLogEntries(@RequestBody List<StoreLogEntryRequest> logEntries) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<LogEntryDto> newEntries = new ArrayList<>();
        for (StoreLogEntryRequest logEntry : logEntries) {
            slt.database.model.LogEntry entry = new slt.database.model.LogEntry();
            entry.setPortionId(logEntry.getPortionId());
            entry.setFoodId(logEntry.getFoodId());
            entry.setMultiplier(logEntry.getMultiplier());
            entry.setDay(new Date(logEntry.getDay().getTime()));
            entry.setMeal(logEntry.getMeal());
            entry.setId(logEntry.getId());
            if (logEntry.getId() == null) {
                logEntryRepository.insertLogEntry(userInfo.getUserId(), entry);
                List<LogEntry> addedEntryMatches = logEntryRepository.getLogEntry(userInfo.getUserId(), entry.getFoodId(), entry.getDay(), entry.getMeal());
                if (addedEntryMatches.size() > 1) { // same food, but logged twice with maybe different portions
                    LogEntry newestEntry = addedEntryMatches.stream().max(Comparator.comparing(LogEntry::getId)).orElse(addedEntryMatches.get(addedEntryMatches.size() - 1));
                    addedEntryMatches = new ArrayList<>();
                    addedEntryMatches.add(newestEntry);
                }
                if (addedEntryMatches.size() != 1) {
                    log.error("SAVE OF ENTRY NOT SUCCEEDED " + userInfo.getUserId() + " - " + entry.getFoodId() + " - " + entry.getDay());
                }
                newEntries.add(mapToDto(userInfo, addedEntryMatches.get(0)));
            } else {
                logEntryRepository.updateLogEntry(userInfo.getUserId(), entry);
                newEntries.add(mapToDto(userInfo, entry));
            }
        }

        return ResponseEntity.ok(newEntries);
    }

    @ApiOperation(value = "Delete logentry")
    @DeleteMapping(path = "/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteLogEntry(@PathVariable("id") Long logEntryId) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        logEntryRepository.deleteLogEntry(userInfo.getUserId(), logEntryId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping(path = "/macros", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getMacrosFromPeriod(@RequestParam("from") String fromDate, @RequestParam("to") String toDate) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date parsedFromDate;
        java.util.Date parsedToDate;
        try {
            parsedFromDate = sdf.parse(fromDate);
            parsedToDate = sdf.parse(toDate);
        } catch (ParseException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        List<LogEntry> allLogEntries = logEntryRepository.getAllLogEntries(userInfo.getUserId(), parsedFromDate, parsedToDate);

        List<LogEntryDto> logEntryDtos = transformToDtos(allLogEntries, userInfo);
        log.debug("Aantal dtos: " + logEntryDtos.size());

        Map<java.util.Date, Optional<LogEntryDto>> collect = logEntryDtos.stream().collect(Collectors.groupingBy(LogEntryDto::getDay, Collectors.reducing((LogEntryDto d1, LogEntryDto d2) -> {
            LogEntryDto logEntryDto = new LogEntryDto();
            logEntryDto.setMacrosCalculated(d1.getMacrosCalculated());
            logEntryDto.getMacrosCalculated().combine(d2.getMacrosCalculated());
            return logEntryDto;
        })));

        List<DayMacro> retObject = new ArrayList<>();
        for (java.util.Date date : collect.keySet()) {
            DayMacro dm = new DayMacro();
            dm.setDay(date);
            Optional<LogEntryDto> logEntryDtoOfDate = collect.get(date);
            if (logEntryDtoOfDate.isPresent()){
                dm.setMacro(logEntryDtoOfDate.get().getMacrosCalculated());
            }
            retObject.add(dm);
        }
        retObject.sort(Comparator.comparing(DayMacro::getDay));

        return ResponseEntity.ok(retObject);
    }

    private List<LogEntryDto> mapToDtos(UserInfo userInfo, List<LogEntry> allLogEntries) {
        List<LogEntryDto> allDtos = new ArrayList<>();
        for (slt.database.model.LogEntry logEntry : allLogEntries) {
            LogEntryDto dto = mapToDto(userInfo, logEntry);
            allDtos.add(dto);
        }
        return allDtos;
    }

    private LogEntryDto mapToDto(UserInfo userInfo, LogEntry logEntry) {
        LogEntryDto dto = new LogEntryDto();
        Food food = foodRepository.getFoodById(userInfo.getUserId(), logEntry.getFoodId());
        dto.setId(logEntry.getId());
        FoodDto foodDto = FoodService.mapFoodToFoodDto(food);
        List<Portion> portions = portionRepository.getPortions(food.getId());
        List<PortionDto> portionDtos = new ArrayList<>();

        Portion portion = null;
        if (portions != null) {
            portionDtos = portions.stream().map(p -> new PortionDto(p.getId(), p.getDescription(), p.getGrams())).collect(Collectors.toList());

            if (logEntry.getPortionId() != null && logEntry.getPortionId() != 0) {
                portion = portionRepository.getPortion(food.getId(), logEntry.getPortionId());
                PortionDto portionDto = new PortionDto(portion.getId(), portion.getDescription(), portion.getGrams());
                Macro calculatedMacros = FoodService.calculateMacro(food, portion);
                portionDto.setMacros(calculatedMacros);
                dto.setPortion(portionDto);
            }
        }
        foodDto.setPortions(portionDtos);
        dto.setFood(foodDto);

        Double multiplier = logEntry.getMultiplier();
        dto.setMultiplier(multiplier);
        dto.setDay(logEntry.getDay());
        dto.setMeal(logEntry.getMeal());

        Macro macrosCalculated = new Macro();
        if (portion != null) {
            macrosCalculated = dto.getPortion().getMacros().createCopy();
            macrosCalculated.multiply(multiplier);

        } else {
            macrosCalculated.setCarbs(multiplier * food.getCarbs());
            macrosCalculated.setFat(multiplier * food.getFat());
            macrosCalculated.setProtein(multiplier * food.getProtein());
        }
        dto.setMacrosCalculated(macrosCalculated);
        return dto;
    }

    /**
     * REFACTOR ONDERSTAANDE NAAR COMMON USAGE BIJ EXPORT
     */

    private List<LogEntryDto> transformToDtos(List<slt.database.model.LogEntry> allLogEntries, UserInfo userInfo) {
        List<Food> allFood = foodRepository.getAllFood(userInfo.getUserId());
        log.info("Export: allFood size = " + allFood.size());
        List<FoodDto> allFoodDtos = new ArrayList<>();
        for (Food food : allFood) {
            allFoodDtos.add(createFoodDto(food, true));
        }
        log.info("Export: allFoodDtos size = " + allFoodDtos.size());


        List<LogEntryDto> allDtos = new ArrayList<>();
        for (slt.database.model.LogEntry logEntry : allLogEntries) {

            LogEntryDto logEntryDto = new LogEntryDto();
            logEntryDto.setId(logEntry.getId());

            FoodDto foodDto = allFoodDtos.stream().filter(f -> f.getId().equals(logEntry.getFoodId())).findFirst().orElseGet(() ->
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
        return allDtos;
    }

    public FoodDto createFoodDto(Food food, boolean withPortions) {
        FoodDto foodDto = mapFoodToFoodDto(food);

        if (withPortions) {
            List<slt.database.model.Portion> foodPortions = portionRepository.getPortions(food.getId());
            for (slt.database.model.Portion portion : foodPortions) {
                PortionDto currDto = new PortionDto();
                currDto.setDescription(portion.getDescription());
                currDto.setGrams(portion.getGrams());
                currDto.setId(portion.getId());

                foodDto.addPortion(currDto);
            }
        }
        return foodDto;
    }


    public static FoodDto mapFoodToFoodDto(Food food) {
        FoodDto foodDto = new FoodDto();
        foodDto.setName(food.getName());
        foodDto.setId(food.getId());
        foodDto.setProtein(food.getProtein());
        foodDto.setCarbs(food.getCarbs());
        foodDto.setFat(food.getFat());
        return foodDto;
    }
}
