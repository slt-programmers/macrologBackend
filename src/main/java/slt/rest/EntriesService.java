package slt.rest;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.database.LogEntryRepository;
import slt.database.entities.LogEntry;
import slt.dto.*;
import slt.mapper.MyModelMapper;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.util.LocalDateParser;
import slt.util.MacroUtils;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/logs")
@Slf4j
public class EntriesService {

    @Autowired
    private LogEntryRepository logEntryRepository;

    @Autowired
    private MyModelMapper myModelMapper;

    @GetMapping(path = "/day/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EntryDto>> getLogEntriesForDay(@PathVariable("date") String date) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        log.debug("Request for " + userInfo);
        LocalDate parsedDate = LocalDateParser.parse(date);

        List<LogEntry> allLogEntries = logEntryRepository.getAllLogEntries(userInfo.getUserId(), parsedDate);
        List<EntryDto> entryDtos = mapToDtos(allLogEntries);

        return ResponseEntity.ok(entryDtos);
    }

    @PostMapping(path = "/day/{date}/{meal}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EntryDto>> postEntries(
            @PathVariable("date") String date,
            @PathVariable("meal") String meal,
            @RequestBody List<EntryDto> entries) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<LogEntry> existingEntriesForMeal = logEntryRepository.getAllLogEntries(
                userInfo.getUserId(),
                LocalDateParser.parse(date),
                meal);
        ModelMapper mapper = myModelMapper.getConfiguredMapper();

        // Delete old
        for (LogEntry existingEntry : existingEntriesForMeal) {
            List<Long> idsUitRequest = entries.stream().map(EntryDto::getId)
                    .toList();
            boolean entryVerwijderd = !idsUitRequest.contains(existingEntry.getId());
            if (entryVerwijderd) {
                logEntryRepository.deleteLogEntry(userInfo.getUserId(), existingEntry.getId());
                log.info("Deleting old existingEntry {}", existingEntry.getFoodId());
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
            entryDto.setMacrosCalculated(MacroUtils.add(entryDto.getMacrosCalculated(), d2.getMacrosCalculated()));
            return entryDto;
        })));

        List<DayMacroDto> retObject = new ArrayList<>();
        for (Map.Entry<java.util.Date, Optional<EntryDto>> dateOptionalEntry : collect.entrySet()) {
            DayMacroDto dm = new DayMacroDto();
            dm.setDay(dateOptionalEntry.getKey());
            Optional<EntryDto> optionalValue = dateOptionalEntry.getValue();
            if (optionalValue.isPresent()) {
                EntryDto entryDto = optionalValue.get();
                dm.setMacros(entryDto.getMacrosCalculated());
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
