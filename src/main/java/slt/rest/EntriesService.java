package slt.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.database.FoodRepository;
import slt.database.LogEntryRepository;
import slt.database.PortionRepository;
import slt.database.entities.LogEntry;
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
public class EntriesService {

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
    public ResponseEntity<List<EntryDto>> getLogEntriesForDay(@PathVariable("date") String date) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        log.debug("Request for " + userInfo);
        LocalDate parsedDate = LocalDateParser.parse(date);

        List<LogEntry> allLogEntries = logEntryRepository.getAllLogEntries(userInfo.getUserId(), parsedDate);
        List<EntryDto> entryDtos = mapToDtos(allLogEntries);

        return ResponseEntity.ok(entryDtos);
    }

    @ApiOperation(value = "Post entries")
    @PostMapping(path = "/day/{date}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EntryDto>> postEntries(
            @PathVariable("date") String date,
            @RequestBody List<EntryDto> entries) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<LogEntry> existingEntries = logEntryRepository.getAllLogEntries(userInfo.getUserId(), LocalDateParser.parse(date));
        ModelMapper mapper = myModelMapper.getConfiguredMapper();

        // Delete old
        for (LogEntry entry : existingEntries) {
            if (!entries.stream().map(e -> e.getFood().getId())
                    .collect(Collectors.toList()).contains(entry.getId())) {
                logEntryRepository.deleteLogEntry(userInfo.getUserId(), entry.getId());
            }
        }

        // Add or update
        for (EntryDto entry : entries) {
            LogEntry entity = mapper.map(entry, LogEntry.class);
            logEntryRepository.saveLogEntry(userInfo.getUserId(), entity);
        }
        List<LogEntry> allEntities = logEntryRepository.getAllLogEntries(userInfo.getUserId(), LocalDateParser.parse(date));
        List<EntryDto> allEntries = allEntities.stream()
                .map(entity -> mapper.map(entity, EntryDto.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(allEntries);
    }

    @Deprecated
    @ApiOperation(value = "Store logentries")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EntryDto>> storeLogEntries(@RequestBody List<LogEntryRequest> logEntries) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<EntryDto> newEntries = new ArrayList<>();
        for (LogEntryRequest logEntry : logEntries) {
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
                // Waarom 0???
                EntryDto map = myModelMapper.getConfiguredMapper().map(addedEntryMatches.get(0), EntryDto.class);
                newEntries.add(map);
            } else {
                logEntryRepository.saveLogEntry(userInfo.getUserId(), entry);
                EntryDto map = myModelMapper.getConfiguredMapper().map(entry, EntryDto.class);
                newEntries.add(map);
            }
        }
        return ResponseEntity.ok(newEntries);
    }

    @ApiOperation(value = "Delete logentry")
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteLogEntry(@PathVariable("id") Long logEntryId) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        logEntryRepository.deleteLogEntry(userInfo.getUserId(), logEntryId);
        ResponseEntity.ok().build();
    }

    @GetMapping(path = "/macros", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DayMacroDto>> getMacrosFromPeriod(@RequestParam("from") String fromDate, @RequestParam("to") String toDate) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();

        LocalDate parsedFromDate = LocalDateParser.parse(fromDate);
        LocalDate parsedToDate = LocalDateParser.parse(toDate);
        List<LogEntry> allLogEntries = logEntryRepository.getAllLogEntries(userInfo.getUserId(), Date.valueOf(parsedFromDate), Date.valueOf(parsedToDate));

        List<EntryDto> entryDtos = mapToDtos(allLogEntries);
        log.debug("Aantal dtos: " + entryDtos.size());

        Map<java.util.Date, Optional<EntryDto>> collect = entryDtos.stream().collect(Collectors.groupingBy(EntryDto::getDay, Collectors.reducing((EntryDto d1, EntryDto d2) -> {
            EntryDto entryDto = new EntryDto();
            entryDto.setMacrosCalculated(d1.getMacrosCalculated());
            entryDto.getMacrosCalculated().combine(d2.getMacrosCalculated());
            return entryDto;
        })));

        List<DayMacroDto> retObject = new ArrayList<>();
        for (Map.Entry<java.util.Date, Optional<EntryDto>> dateOptionalEntry : collect.entrySet()) {
            DayMacroDto dm = new DayMacroDto();
            dm.setDay(dateOptionalEntry.getKey());
            Optional<EntryDto> optionalValue = dateOptionalEntry.getValue();
            if (optionalValue.isPresent()) {
                EntryDto entryDto = optionalValue.get();
                dm.setMacro(entryDto.getMacrosCalculated());
            }
            retObject.add(dm);
        }
        retObject.sort(Comparator.comparing(DayMacroDto::getDay));

        return ResponseEntity.ok(retObject);
    }

    private List<EntryDto> mapToDtos(List<LogEntry> allLogEntries) {
        return allLogEntries.stream().map(
                l -> myModelMapper.getConfiguredMapper().map(l, EntryDto.class))
                .collect(Collectors.toList());
    }

}
