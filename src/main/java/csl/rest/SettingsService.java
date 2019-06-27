package csl.rest;

import csl.database.SettingsRepository;
import csl.database.WeightRepository;
import csl.database.model.Setting;
import csl.database.model.Weight;
import csl.dto.UserSettingsDto;
import csl.dto.WeightDto;
import csl.security.ThreadLocalHolder;
import csl.security.UserInfo;
import csl.util.LocalDateParser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jdk.nashorn.internal.objects.annotations.Setter;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.jni.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping("/settings")
@Api(value = "settings")
public class SettingsService {

    private SettingsRepository settingsRepo = new SettingsRepository();

    private WeightRepository weightRepo = new WeightRepository();

    private WeightService weightService = new WeightService();


    @ApiOperation(value = "Store new setting or change existing one")
    @RequestMapping(value = "",
            method = PUT,
            headers = {"Content-Type=application/json"})
    public ResponseEntity changeSetting(@RequestBody Setting setting) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        if ("weight".equals(setting.getName())) {
            weightService.storeWeightEntry(
                    new WeightDto(null,
                            Double.valueOf(setting.getValue()),
                            setting.getDay()==null?LocalDate.now():setting.getDay().toLocalDate(),
                    null));
        }
        settingsRepo.putSetting(userInfo.getUserId(), setting.getName(), setting.getValue(),setting.getDay()==null?Date.valueOf(LocalDate.now()):setting.getDay());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ApiOperation(value = "Get all settings")
    @RequestMapping(value = "",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllSetting() {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();

        List<Setting> settings = settingsRepo.getAllSettings(userInfo.getUserId());
        // todo is this used? ja voor personal page, maar die krijgt nu teveel. Ombouwen om /user te gaan gebruiken
        return ResponseEntity.ok(settings);
    }

    @ApiOperation(value = "Get user settings")
    @RequestMapping(value = "/user",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getUserSetting() {
        UserSettingsDto userSettingsDto = getUserSettingsDto();
        return ResponseEntity.ok(userSettingsDto);
    }

    private UserSettingsDto getUserSettingsDto() {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<Setting> settings = settingsRepo.getAllSettings(userInfo.getUserId());
        List<Weight> weight = weightRepo.getAllWeightEntries(userInfo.getUserId());
        Weight currentWeight = weight.stream().max(Comparator.comparing(Weight::getDay)).orElse(new Weight());
        UserSettingsDto userSettingsDto = mapToUserSettingsDto(settings);
        userSettingsDto.setCurrentWeight(currentWeight.getWeight());
        return userSettingsDto;
    }

    @ApiOperation(value = "Get setting")
    @RequestMapping(value = "/{name}",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getSetting(@PathVariable("name") String name,
                                     @RequestParam(value = "date",required = false) String toDate) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        Setting setting = null;
        if (StringUtils.isEmpty(toDate)) {
            setting = settingsRepo.getLatestSetting(userInfo.getUserId(), name);
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date utilDate = sdf.parse(toDate);
                setting = settingsRepo.getValidSetting(userInfo.getUserId(), name, new Date(utilDate.getTime()));
            } catch (ParseException pe) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }
        return ResponseEntity.ok(setting==null?null:setting.getValue());
    }

    private UserSettingsDto mapToUserSettingsDto(List<Setting> settings) {
        UserSettingsDto dto = new UserSettingsDto();
        dto.setName(mapSetting(settings, "name"));
        dto.setGender(mapSetting(settings, "gender"));
        dto.setAge(Integer.valueOf(mapSetting(settings, "age")));
        dto.setBirthday(LocalDateParser.parse(mapSetting(settings, "birthday")));
        dto.setHeight(Integer.valueOf(mapSetting(settings, "height")));
        dto.setActivity(Double.valueOf(mapSetting(settings, "activity")));

        if (settingsContainGoals(settings)) {
            dto.setGoalProtein(Integer.valueOf(mapSetting(settings, "goalProtein")));
            dto.setGoalFat(Integer.valueOf(mapSetting(settings, "goalFat")));
            dto.setGoalCarbs(Integer.valueOf(mapSetting(settings, "goalCarbs")));
        }
        return dto;
    }

    private String mapSetting(List<Setting> settings, String identifier) {
        return settings.stream().filter(s -> s.getName().equals(identifier)).max(Comparator.comparing(Setting::getDay)).orElse(new Setting()).getValue();
    }

    private boolean settingsContainGoals(List<Setting> settings) {
        for (Setting setting : settings) {
            if (setting.getName().contains("goal")) {
                return true;
            }
        }
        return false;
    }

}
