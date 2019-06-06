package csl.rest;

import csl.database.FoodRepository;
import csl.database.LogEntryRepository;
import csl.database.PortionRepository;
import csl.database.model.Food;
import csl.database.model.LogEntry;
import csl.database.model.Portion;
import csl.dto.*;
import csl.security.ThreadLocalHolder;
import csl.security.UserInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/logs")
@Api(value = "logs", description = "Operations pertaining to logentries in the macro logger application")
public class LogEntryService {

    private FoodRepository foodRepository = new FoodRepository();
    private PortionRepository portionRepository = new PortionRepository();
    private LogEntryRepository logEntryRepository = new LogEntryRepository();
    private static final Logger LOGGER = LoggerFactory.getLogger(LogEntryService.class);

    @ApiOperation(value = "Retrieve all stored logentries")
    @RequestMapping(value = "",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllLogEntries() {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<csl.database.model.LogEntry> allLogEntries = logEntryRepository.getAllLogEntries(userInfo.getUserId());

        return ResponseEntity.ok(mapToDtos(userInfo, allLogEntries));
    }

    @ApiOperation(value = "Retrieve all stored logentries for date")
    @RequestMapping(value = "/day/{date}",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getLogEntriesForDay(@PathVariable("date") String date) {

        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        LOGGER.debug("Request for " + userInfo);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date parsedDate;
        try {
            parsedDate = sdf.parse(date);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        List<csl.database.model.LogEntry> allLogEntries = logEntryRepository.getAllLogEntries(userInfo.getUserId(), parsedDate);
        List<LogEntryDto> logEntryDtos = mapToDtos(userInfo, allLogEntries);

        return ResponseEntity.ok(logEntryDtos);
    }

    @ApiOperation(value = "Store logentries")
    @RequestMapping(value = "",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity storeLogEntries(@RequestBody List<StoreLogEntryRequest> logEntries) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<LogEntryDto> newEntries = new ArrayList<>();
        for (StoreLogEntryRequest logEntry : logEntries) {
            csl.database.model.LogEntry entry = new csl.database.model.LogEntry();
            entry.setPortionId(logEntry.getPortionId());
            entry.setFoodId(logEntry.getFoodId());
            entry.setMultiplier(logEntry.getMultiplier());
            entry.setDay(new Date(logEntry.getDay().getTime()));
            entry.setMeal(logEntry.getMeal());
            entry.setId(logEntry.getId());
            if (logEntry.getId() == null) {
                logEntryRepository.insertLogEntry(userInfo.getUserId(), entry);
                List<LogEntry> addedEntryMatches = logEntryRepository.getLogEntry(userInfo.getUserId(), entry.getFoodId(), entry.getDay(), entry.getMeal());
                if (addedEntryMatches.size() > 1) {
                    LogEntry newestEntry = addedEntryMatches.stream().max(Comparator.comparing(LogEntry::getId)).orElse(addedEntryMatches.get(addedEntryMatches.size() -1));
                    addedEntryMatches = new ArrayList<>();
                    addedEntryMatches.add(newestEntry);
                }
                if (addedEntryMatches.size() != 1) {
                    LOGGER.error("SAVE OF ENTRY NOT SUCCEEDED " + userInfo.getUserId() + " - " + entry.getFoodId() + " - " + entry.getDay());
                }
                newEntries.add(mapToDto(userInfo, addedEntryMatches.get(0)));
            } else {
                logEntryRepository.updateLogEntry(userInfo.getUserId(), entry);
                newEntries.add(mapToDto(userInfo,entry));
            }
        }

        return ResponseEntity.ok(newEntries);
    }

    @ApiOperation(value = "Delete logentry")
    @RequestMapping(value = "/{id}",
            method = DELETE,
            headers = {"Content-Type=application/json"})
    public ResponseEntity storeLogEntry(@PathVariable("id") Long logEntryId) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        logEntryRepository.deleteLogEntry(userInfo.getUserId(), logEntryId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping(value = "/macros",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getMacrosFromPeriod(@RequestParam("from") String fromDate, @RequestParam("to") String toDate) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date parsedFromDate;
        java.util.Date parsedToDate;
        try {
            parsedFromDate = sdf.parse(fromDate);
            parsedToDate = sdf.parse(toDate);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }


        List<LogEntry> allLogEntries = logEntryRepository.getAllLogEntries(userInfo.getUserId(), parsedFromDate, parsedToDate);
        List<LogEntryDto> logEntryDtos = mapToDtos(userInfo, allLogEntries);
        LOGGER.debug("Aantal dtos:" + logEntryDtos.size());

        Map<java.util.Date, Optional<LogEntryDto>> collect = logEntryDtos.stream().collect(Collectors.groupingBy(LogEntryDto::getDay, Collectors.reducing((LogEntryDto d1, LogEntryDto d2) -> {
            LogEntryDto d3 = new LogEntryDto();
            d3.setMacrosCalculated(d1.getMacrosCalculated());
            d3.getMacrosCalculated().combine(d2.getMacrosCalculated());
            return d3;
        })));

        ArrayList<DayMacro> retObject = new ArrayList<>();
        for (java.util.Date date : collect.keySet()) {
            DayMacro dm = new DayMacro();
            dm.setDay(date);
            dm.setMacro(collect.get(date).get().getMacrosCalculated());
            retObject.add(dm);
        }
        Collections.sort(retObject, Comparator.comparing(DayMacro::getDay));

        return ResponseEntity.ok(retObject);
    }

    private List<LogEntryDto> mapToDtos(UserInfo userInfo, List<LogEntry> allLogEntries) {
        List<LogEntryDto> allDtos = new ArrayList<>();
        for (csl.database.model.LogEntry logEntry : allLogEntries) {

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
            macrosCalculated = dto.getPortion().getMacros().clone();
            macrosCalculated.multiply(multiplier);

        } else {
            macrosCalculated.setCarbs(multiplier * food.getCarbs());
            macrosCalculated.setFat(multiplier * food.getFat());
            macrosCalculated.setProtein(multiplier * food.getProtein());
        }
        dto.setMacrosCalculated(macrosCalculated);
        return dto;
    }
}
