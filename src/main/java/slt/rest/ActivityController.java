package slt.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.dto.ActivityDto;
import slt.security.ThreadLocalHolder;
import slt.service.ActivityService;
import slt.util.LocalDateParser;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/activities")
@AllArgsConstructor
public class ActivityController {

    private ActivityService activityService;

    @GetMapping(path = "/day/{date}")
    public ResponseEntity<List<ActivityDto>> getActivitiesForDay(@PathVariable("date") String date,
                                                                 @RequestParam(value = "forceSync", defaultValue = "false") boolean forceSync) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var localDate = LocalDateParser.parse(date);

        final var allActivities = activityService.getActivitiesForDay(userInfo.getUserId(), localDate, forceSync);
        return ResponseEntity.ok(allActivities);
    }

    @PostMapping(path = "/day/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ActivityDto>> postActivities(@PathVariable("date") final String date, @RequestBody final List<ActivityDto> activities) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var localDate = LocalDateParser.parse(date);
        final var allActivities = activityService.postActivities(userInfo.getUserId(), localDate, activities);
        return ResponseEntity.ok(allActivities);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable("id") final Long activityId) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        activityService.deleteActivity(userInfo.getUserId(), activityId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
