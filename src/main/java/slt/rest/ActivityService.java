package slt.rest;


import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.connectivity.strava.StravaActivityService;
import slt.database.ActivityRepository;
import slt.database.entities.LogActivity;
import slt.dto.LogActivityDto;
import slt.mapper.MyModelMapper;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.util.LocalDateParser;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/activities")
public class ActivityService {

    @Autowired
    private ActivityRepository logActivityRepository;

    @Autowired
    private MyModelMapper myModelMapper;

    @Autowired
    private StravaActivityService stravaActivityService;

    @GetMapping(path = "/day/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LogActivityDto>> getActivitiesForDay(@PathVariable("date") String date,
                                              @RequestParam(value = "forceSync", defaultValue = "false") boolean forceSync) {

        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        log.debug("Request for " + userInfo);
        LocalDate localDate = LocalDateParser.parse(date);

        List<LogActivity> allLogEntries = logActivityRepository.getAllLogActivities(userInfo.getUserId(), localDate);

        List<LogActivity> extraSynced = stravaActivityService.getExtraStravaActivities(allLogEntries, userInfo.getUserId(), localDate, forceSync);
        allLogEntries.addAll(extraSynced);

        List<LogActivityDto> logEntryDtos = allLogEntries.stream()
                .filter(logActivity -> !"DELETED".equals(logActivity.getStatus()))
                .map(logEntry -> myModelMapper.getConfiguredMapper().map(logEntry, LogActivityDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(logEntryDtos);
    }

    @PostMapping(path = "/day/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LogActivityDto>> postActivities(
            @PathVariable("date") String date,
            @RequestBody List<LogActivityDto> activities) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        Integer userId = userInfo.getUserId();
        ModelMapper mapper = myModelMapper.getConfiguredMapper();

        List<LogActivity> existingActs = logActivityRepository
                .getAllLogActivities(userId, LocalDateParser.parse(date));

        // Delete old
        for (LogActivity act : existingActs) {
            if (!activities.stream().map(LogActivityDto::getId)
                    .collect(Collectors.toList()).contains(act.getId())) {
                logActivityRepository.deleteLogActivity(userId, act.getId());
            }
        }

        // Add or update
        for (LogActivityDto act : activities) {
            LogActivity entity = mapper.map(act, LogActivity.class);
            logActivityRepository.saveActivity(userId, entity);
        }
        List<LogActivity> allEntities = logActivityRepository.getAllLogActivities(userId, LocalDateParser.parse(date));
        List<LogActivityDto> allActs = allEntities.stream()
                .map(entity -> mapper.map(entity, LogActivityDto.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(allActs);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteActivity(@PathVariable("id") Long logEntryId) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        logActivityRepository.deleteLogActivity(userInfo.getUserId(), logEntryId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
