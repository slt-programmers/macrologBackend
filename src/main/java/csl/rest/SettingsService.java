package csl.rest;

import csl.database.SettingsRepository;
import csl.database.model.Setting;
import csl.security.ThreadLocalHolder;
import csl.security.UserInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/settings")
@Api(value = "settings", description = "Operations pertaining personal settings")
public class SettingsService {

    private SettingsRepository settingsRepo = new SettingsRepository();

    @ApiOperation(value = "Store new setting or change existing one")
    @RequestMapping(value = "",
            method = PUT,
            headers = {"Content-Type=application/json"})
    public ResponseEntity changeSetting(@RequestBody Setting setting) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        settingsRepo.putSetting(userInfo.getUserId(),setting.getName(), setting.getValue());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ApiOperation(value = "Get all settings")
    @RequestMapping(value = "",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllSetting() {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<Setting> settings = settingsRepo.getAllSettings(userInfo.getUserId());
        return ResponseEntity.ok(settings);
    }

    @ApiOperation(value = "Get setting")
    @RequestMapping(value = "/{name}",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getSetting(@PathVariable("name") String name) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        String setting = settingsRepo.getSetting(userInfo.getUserId(),name);
        return ResponseEntity.ok(setting);
    }

    @ApiOperation(value = "Post new weight")
    @RequestMapping(value = "/weight",
            method = POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity insertWeight(@RequestParam("weight") String weight,
                                       @RequestParam("date") String date) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        LocalDate localDate = LocalDate.parse(date);
        System.out.println(localDate);
        Date sqlDate = new Date(localDate.toEpochDay());
        settingsRepo.insertSetting(userInfo.getUserId(), "weight", weight, sqlDate);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
