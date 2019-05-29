package csl.rest;

import csl.database.ActivityRepository;
import csl.database.model.LogActivity;
import csl.dto.LogActivityDto;
import csl.dto.LogEntryDto;
import csl.security.ThreadLocalHolder;
import csl.security.UserInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/activities")
@Api(value = "logs", description = "Operations pertaining to activities in the macro logger application")
public class ActivityService {

    private ActivityRepository logActitivyRepository = new ActivityRepository();
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityService.class);

    @ApiOperation(value = "Retrieve all stored activities for date")
    @RequestMapping(value = "/day/{date}",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getActivitiesForDay(@PathVariable("date") String date) {

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

        List<LogActivity> allLogEntries = logActitivyRepository.getAllLogActivities(userInfo.getUserId(), parsedDate);
        List<LogActivityDto> logEntryDtos = mapToDtos(userInfo, allLogEntries);

        return ResponseEntity.ok(logEntryDtos);
    }

    @ApiOperation(value = "Store activities")
    @RequestMapping(value = "",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity storeActivities(@RequestBody List<LogActivityDto> logActivities) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<LogActivityDto> newEntries = new ArrayList<>();
        for (LogActivityDto logEntry : logActivities) {
            LogActivity entry = new LogActivity();
            entry.setName(logEntry.getName());
            entry.setCalories(logEntry.getCalories());
            entry.setDay(new Date(logEntry.getDay().getTime()));
            entry.setId(logEntry.getId());
            if (logEntry.getId() == null) {
                logActitivyRepository.insertActivity(userInfo.getUserId(), entry);
                List<LogActivity> newEntry = logActitivyRepository.getAllLogActivities(userInfo.getUserId(), entry.getDay());
                if (newEntry.size() > 1) {
                    LogActivity newestEntry = newEntry.stream().max(Comparator.comparing(LogActivity::getId)).orElse(newEntry.get(newEntry.size() - 1));
                    newEntry = new ArrayList<>();
                    newEntry.add(newestEntry);
                }
                newEntries = mapToDtos(userInfo, newEntry);
            } else {
                // TODO BUG? Add to list of new entries? Why only new?
                logActitivyRepository.updateLogActivity(userInfo.getUserId(), entry);
            }
        }

        return ResponseEntity.ok(newEntries);
    }

    @ApiOperation(value = "Delete activity")
    @RequestMapping(value = "/{id}",
            method = DELETE,
            headers = {"Content-Type=application/json"})
    public ResponseEntity storeActivity(@PathVariable("id") Long logEntryId) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        logActitivyRepository.deleteLogActivity(userInfo.getUserId(), logEntryId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    private List<LogActivityDto> mapToDtos(UserInfo userInfo, List<LogActivity> allLogActivities) {
        List<LogActivityDto> allDtos = new ArrayList<>();
        for (LogActivity logEntry : allLogActivities) {

            LogActivityDto dto = new LogActivityDto();
            dto.setCalories(logEntry.getCalories());
            dto.setName(logEntry.getName());
            dto.setId(logEntry.getId());
            dto.setDay(logEntry.getDay());
            allDtos.add(dto);
        }

        return allDtos;
    }
}
