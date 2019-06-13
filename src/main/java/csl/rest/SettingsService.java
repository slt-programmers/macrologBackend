package csl.rest;

import csl.database.SettingsRepository;
import csl.database.WeightRepository;
import csl.database.model.Setting;
import csl.database.model.Weight;
import csl.dto.UserSettingsDto;
import csl.security.ThreadLocalHolder;
import csl.security.UserInfo;
import csl.util.LocalDateParser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/settings")
@Api(value = "settings")
public class SettingsService {

    private SettingsRepository settingsRepo = new SettingsRepository();

    private WeightRepository weightRepo = new WeightRepository();

    @ApiOperation(value = "Store new setting or change existing one")
    @RequestMapping(value = "",
            method = PUT,
            headers = {"Content-Type=application/json"})
    public ResponseEntity changeSetting(@RequestBody Setting setting) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        settingsRepo.putSetting(userInfo.getUserId(), setting.getName(), setting.getValue());
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

    @ApiOperation(value = "Get user settings")
    @RequestMapping(value = "/user",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getUserSetting() {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<Setting> settings = settingsRepo.getAllSettings(userInfo.getUserId());
        List<Weight> weight = weightRepo.getAllWeightEntries(userInfo.getUserId());
        Weight currentWeight = weight.stream().max(Comparator.comparing(Weight::getDay)).orElse(new Weight());
        UserSettingsDto userSettingsDto = mapToUserSettingsDto(settings);
        userSettingsDto.setCurrentWeight(currentWeight.getWeight());
        return ResponseEntity.ok(userSettingsDto);
    }

    private UserSettingsDto mapToUserSettingsDto(List<Setting> settings) {
        UserSettingsDto dto = new UserSettingsDto();
        dto.setName(mapSetting(settings, "name"));
        dto.setGender(mapSetting(settings, "gender"));
        dto.setAge(Integer.valueOf(mapSetting(settings, "age")));
        dto.setBirthday(LocalDateParser.parse(mapSetting(settings, "birthday")));
        dto.setHeight(Integer.valueOf(mapSetting(settings, "height")));
        dto.setActivity(Double.valueOf(mapSetting(settings, "activity")));
        dto.setGoalProtein(Integer.valueOf(mapSetting(settings, "goalProtein")));
        dto.setGoalFat(Integer.valueOf(mapSetting(settings, "goalFat")));
        dto.setGoalCarbs(Integer.valueOf(mapSetting(settings, "goalCarbs")));

        return dto;
    }

    private String mapSetting(List<Setting> settings, String identifier) {
        return settings.stream().filter(s -> s.getName().equals(identifier)).findFirst().orElse(new Setting()).getValue();
    }


    @ApiOperation(value = "Get setting")
    @RequestMapping(value = "/{name}",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getSetting(@PathVariable("name") String name) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        String setting = settingsRepo.getSetting(userInfo.getUserId(), name);
        return ResponseEntity.ok(setting);
    }

}
