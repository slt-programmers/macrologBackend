package slt.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.database.FoodRepository;
import slt.database.LogEntryRepository;
import slt.database.PortionRepository;
import slt.database.entities.Food;
import slt.database.entities.LogEntry;
import slt.database.entities.Portion;
import slt.dto.*;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.util.LocalDateParser;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private MyModelMapper myModelMapper;

    @ApiOperation(value = "Retrieve all stored logentries for date")
    @GetMapping(path = "/day/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getLogEntriesForDay(@PathVariable("date") String date) {

        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        log.debug("Request for " + userInfo);
        LocalDate parsedDate = LocalDateParser.parse(date);

        List<LogEntry> allLogEntries = logEntryRepository.getAllLogEntries(userInfo.getUserId(), Date.valueOf(parsedDate));
        List<LogEntryDto> logEntryDtos = mapToDtos(userInfo, allLogEntries);

        return ResponseEntity.ok(logEntryDtos);
    }

    @ApiOperation(value = "Store logentries")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity storeLogEntries(@RequestBody List<StoreLogEntryRequest> logEntries) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<LogEntryDto> newEntries = new ArrayList<>();
        for (StoreLogEntryRequest logEntry : logEntries) {
            LogEntry entry = new LogEntry();
            entry.setPortionId(logEntry.getPortionId());
            entry.setFoodId(logEntry.getFoodId());
            entry.setMultiplier(logEntry.getMultiplier());
            entry.setDay(new Date(logEntry.getDay().getTime()));
            entry.setMeal(logEntry.getMeal());
            entry.setId(logEntry.getId());
            if (logEntry.getId() == null) {
                logEntryRepository.saveLogEntry(userInfo.getUserId(), entry);
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
                logEntryRepository.saveLogEntry(userInfo.getUserId(), entry);
                newEntries.add(mapToDto(userInfo, entry));
            }
        }

        return ResponseEntity.ok(newEntries);
    }

    @ApiOperation(value = "Delete logentry")
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteLogEntry(@PathVariable("id") Long logEntryId) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        logEntryRepository.deleteLogEntry(userInfo.getUserId(), logEntryId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping(path = "/macros", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getMacrosFromPeriod(@RequestParam("from") String fromDate, @RequestParam("to") String toDate) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();

        LocalDate parsedFromDate = LocalDateParser.parse(fromDate);
        LocalDate parsedToDate = LocalDateParser.parse(toDate);
        List<LogEntry> allLogEntries = logEntryRepository.getAllLogEntries(userInfo.getUserId(), Date.valueOf(parsedFromDate), Date.valueOf(parsedToDate));

        List<LogEntryDto> logEntryDtos = transformToDtos(allLogEntries, userInfo);
        log.debug("Aantal dtos: " + logEntryDtos.size());

        Map<java.util.Date, Optional<LogEntryDto>> collect = logEntryDtos.stream().collect(Collectors.groupingBy(LogEntryDto::getDay, Collectors.reducing((LogEntryDto d1, LogEntryDto d2) -> {
            LogEntryDto logEntryDto = new LogEntryDto();
            logEntryDto.setMacrosCalculated(d1.getMacrosCalculated());
            logEntryDto.getMacrosCalculated().combine(d2.getMacrosCalculated());
            return logEntryDto;
        })));

        List<DayMacro> retObject = new ArrayList<>();
        for (Map.Entry<java.util.Date, Optional<LogEntryDto>> dateOptionalEntry : collect.entrySet()) {
            DayMacro dm = new DayMacro();
            dm.setDay(dateOptionalEntry.getKey());
            if (dateOptionalEntry.getValue().isPresent()) {
                dm.setMacro(dateOptionalEntry.getValue().get().getMacrosCalculated());
            }
            retObject.add(dm);
        }
        retObject.sort(Comparator.comparing(DayMacro::getDay));

        return ResponseEntity.ok(retObject);
    }

    private List<LogEntryDto> mapToDtos(UserInfo userInfo, List<LogEntry> allLogEntries) {
        List<LogEntryDto> allDtos = new ArrayList<>();
        for (LogEntry logEntry : allLogEntries) {
            LogEntryDto dto = mapToDto(userInfo, logEntry);
            allDtos.add(dto);
        }
        return allDtos;
    }

    private LogEntryDto mapToDto(UserInfo userInfo, LogEntry logEntry) {
        LogEntryDto dto = new LogEntryDto();
        Food food = foodRepository.getFoodById(userInfo.getUserId(), logEntry.getFoodId());
        dto.setId(logEntry.getId());
        FoodDto foodDto = myModelMapper.getConfiguredMapper().map(food, FoodDto.class);
        List<Portion> portions = portionRepository.getPortions(food.getId());
        List<PortionDto> portionDtos = new ArrayList<>();

        Portion portion = null;
        if (portions != null) {
            portionDtos = portions.stream()
                    .map(p -> myModelMapper.getConfiguredMapper().map(p,PortionDto.class ))
                    .collect(Collectors.toList());

            if (logEntry.getPortionId() != null && logEntry.getPortionId() != 0) {
                portion = portionRepository.getPortion(food.getId(), logEntry.getPortionId());
                PortionDto portionDto =myModelMapper.getConfiguredMapper().map(portion,PortionDto.class);
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

    private List<LogEntryDto> transformToDtos(List<LogEntry> allLogEntries, UserInfo userInfo) {
        List<Food> allFood = foodRepository.getAllFood(userInfo.getUserId());
        log.info("Export: allFood size = " + allFood.size());
        List<FoodDto> allFoodDtos = new ArrayList<>();
        for (Food food : allFood) {
            allFoodDtos.add(createFoodDto(food, true));
        }
        log.info("Export: allFoodDtos size = " + allFoodDtos.size());


        List<LogEntryDto> allDtos = new ArrayList<>();
        for (LogEntry logEntry : allLogEntries) {

            LogEntryDto logEntryDto = new LogEntryDto();
            logEntryDto.setId(logEntry.getId());

            FoodDto foodDto = allFoodDtos
                    .stream()
                    .filter(f -> f.getId().equals(logEntry.getFoodId()))
                    .findFirst()
                    .orElseGet(() -> {
                        Food foodById = foodRepository.getFoodById(userInfo.getUserId(), logEntry.getFoodId());
                        return myModelMapper.getConfiguredMapper().map(foodById, FoodDto.class);
                    });
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
        FoodDto foodDto = myModelMapper.getConfiguredMapper().map(food,FoodDto.class);

        if (withPortions) {
            List<Portion> foodPortions = portionRepository.getPortions(food.getId());
            for (Portion portion : foodPortions) {
                PortionDto currDto = myModelMapper.getConfiguredMapper().map(portion,PortionDto.class);
                foodDto.addPortion(currDto);
            }
        }
        return foodDto;
    }
}
