package slt.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.database.ActivityRepository;
import slt.database.entities.LogActivity;
import slt.dto.LogActivityDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.util.LocalDateParser;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/activities")
@Api(value = "logs")
public class ActivityService {

    @Autowired
    private ActivityRepository logActitivyRepository;

    @ApiOperation(value = "Retrieve all stored activities for date")
    @GetMapping(path = "/day/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getActivitiesForDay(@PathVariable("date") String date) {

        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        log.debug("Request for " + userInfo);
        LocalDate localDate = LocalDateParser.parse(date);
        List<LogActivity> allLogEntries = logActitivyRepository.getAllLogActivities(userInfo.getUserId(), localDate);
        List<LogActivityDto> logEntryDtos = mapToDtos(allLogEntries);

        return ResponseEntity.ok(logEntryDtos);
    }

    @ApiOperation(value = "Store activities")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity storeActivities(@RequestBody List<LogActivityDto> logActivities) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<LogActivityDto> newEntries = new ArrayList<>();
        for (LogActivityDto logEntry : logActivities) {
            LogActivity entry = mapActivityDtoToDomain(logEntry);
            if (logEntry.getId() == null) {
                logActitivyRepository.insertActivity(userInfo.getUserId(), entry);
                List<LogActivity> addedEntryMatches = logActitivyRepository.getAllLogActivities(userInfo.getUserId(), entry.getDay().toLocalDate());
                if (addedEntryMatches.size() > 1) {
                    LogActivity newestEntry = addedEntryMatches.stream().max(Comparator.comparing(LogActivity::getId)).orElse(addedEntryMatches.get(addedEntryMatches.size() - 1));
                    addedEntryMatches = new ArrayList<>();
                    addedEntryMatches.add(newestEntry);
                }
                if (addedEntryMatches.size() != 1) {
                    log.error("SAVE OF ENTRY NOT SUCCEEDED " + userInfo.getUserId() + " - " + entry.getName() + " - " + entry.getDay());
                }
                newEntries.add(mapToDto(addedEntryMatches.get(0)));
            } else {
                // TODO BUG? Add to list of new entries? Why only new?
                logActitivyRepository.updateLogActivity(userInfo.getUserId(), entry);
                newEntries.add(mapToDto(entry));
            }
        }

        return ResponseEntity.ok(newEntries);
    }


    @ApiOperation(value = "Delete activity")
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteActivity(@PathVariable("id") Long logEntryId) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        logActitivyRepository.deleteLogActivity(userInfo.getUserId(), logEntryId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private LogActivity mapActivityDtoToDomain(LogActivityDto logEntry) {
        LogActivity entry = new LogActivity();
        entry.setName(logEntry.getName());
        entry.setCalories(logEntry.getCalories());
        entry.setDay(new Date(logEntry.getDay().getTime()));
        entry.setId(logEntry.getId());
        return entry;
    }

    private List<LogActivityDto> mapToDtos(List<LogActivity> allLogActivities) {
        List<LogActivityDto> allDtos = new ArrayList<>();
        for (LogActivity logEntry : allLogActivities) {

            LogActivityDto dto = mapToDto(logEntry);
            allDtos.add(dto);
        }

        return allDtos;
    }

    private LogActivityDto mapToDto(LogActivity logEntry) {
        LogActivityDto dto = new LogActivityDto();
        dto.setCalories(logEntry.getCalories());
        dto.setName(logEntry.getName());
        dto.setId(logEntry.getId());
        dto.setDay(logEntry.getDay());
        return dto;
    }
}
