package slt.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.connectivity.ListedActivityDto;
import slt.connectivity.StravaActivityService;
import slt.database.ActivityRepository;
import slt.database.entities.LogActivity;
import slt.dto.LogActivityDto;
import slt.dto.MyModelMapper;
import slt.dto.SyncedAccount;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.util.LocalDateParser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/sync")
@Api(value = "sync")
public class SyncService {

    @Autowired
    private StravaActivityService stravaActivityService;

    @ApiOperation(value = "Synchronize with strava account")
    @GetMapping(path = "/syncStrava", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SyncedAccount> syncStrava(@RequestParam("code") String code) {

        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        SyncedAccount syncedAccount = stravaActivityService.registerStravaConnectivity(userInfo.getUserId(), code);

        if (syncedAccount == null) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(syncedAccount);
        }
    }

    @ApiOperation(value = "Get activities from strava for day")
    @GetMapping(path = "/syncStravaDay", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getStravaActivitiesForDay(@RequestParam("date") String date) {

        LocalDate parsedFromDate = LocalDateParser.parse(date);

        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        final List<ListedActivityDto> stravaActivitiesForDay = stravaActivityService.getStravaActivitiesForDay(userInfo.getUserId(), parsedFromDate);

        if (stravaActivitiesForDay == null) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(stravaActivitiesForDay);
        }
    }


    @ApiOperation(value = "Synchronize with strava account")
    @GetMapping(path = "/syncinfo/STRAVA", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SyncedAccount> syncStravaInfo() {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        SyncedAccount syncedAccount = stravaActivityService.getStravaConnectivity(userInfo.getUserId());

        if (syncedAccount == null) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(syncedAccount);
        }

    }
}
