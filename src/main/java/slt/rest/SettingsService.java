package slt.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.database.SettingsRepository;
import slt.database.WeightRepository;
import slt.database.model.Setting;
import slt.database.model.Weight;
import slt.dto.UserSettingsDto;
import slt.dto.WeightDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.util.LocalDateParser;

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

    @Autowired
    private SettingsRepository settingsRepo;

    @Autowired
    private WeightRepository weightRepo;

    @Autowired
    private WeightService weightService;

    @ApiOperation(value = "Store new setting or change existing one")
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity storeSetting(@RequestBody Setting setting) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        if ("weight".equals(setting.getName())) {
            weightService.storeWeightEntry(
                    new WeightDto(null,
                            Double.valueOf(setting.getValue()),
                            setting.getDay() == null ? LocalDate.now() : setting.getDay().toLocalDate(),
                            null));
        }
        settingsRepo.putSetting(userInfo.getUserId(), setting.getName(), setting.getValue(), setting.getDay() == null ? Date.valueOf(LocalDate.now()) : setting.getDay());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ApiOperation(value = "Get user settings")
    @GetMapping(path = "/user",produces = MediaType.APPLICATION_JSON_VALUE)
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
    @GetMapping(path = "/{name}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getSetting(@PathVariable("name") String name,
                                     @RequestParam(value = "date", required = false) String toDate) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        Setting setting;
        if (StringUtils.isEmpty(toDate)) {
            setting = settingsRepo.getLatestSetting(userInfo.getUserId(), name);
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                java.util.Date utilDate = sdf.parse(toDate);
                setting = settingsRepo.getValidSetting(userInfo.getUserId(), name, new Date(utilDate.getTime()));
            } catch (ParseException pe) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }
        return ResponseEntity.ok(setting == null ? null : setting.getValue());
    }

    private UserSettingsDto mapToUserSettingsDto(List<Setting> settings) {
        UserSettingsDto dto = new UserSettingsDto();
        dto.setName(mapSetting(settings, "name"));
        dto.setGender(mapSetting(settings, "gender"));
        String ageSetting = mapSetting(settings, "age");
        dto.setAge(StringUtils.isEmpty(ageSetting)?null:Integer.valueOf(ageSetting));
        String birthdaySetting = mapSetting(settings, "birthday");
        dto.setBirthday(StringUtils.isEmpty(birthdaySetting)?null:LocalDateParser.parse(birthdaySetting));
        String heightSetting = mapSetting(settings, "height");
        dto.setHeight(StringUtils.isEmpty(heightSetting)?null:Integer.valueOf(heightSetting));
        String activitySetting = mapSetting(settings, "activity");
        dto.setActivity(StringUtils.isEmpty(activitySetting)?null:Double.valueOf(activitySetting));

        String goalProteinSetting = mapSetting(settings, "goalProtein");
        dto.setGoalProtein(StringUtils.isEmpty(goalProteinSetting)?null:Integer.valueOf(goalProteinSetting));
        String goalFatSetting = mapSetting(settings, "goalFat");
        dto.setGoalFat(StringUtils.isEmpty(goalFatSetting)?null:Integer.valueOf(goalFatSetting));
        String goalCarbsSetting = mapSetting(settings, "goalCarbs");
        dto.setGoalCarbs(StringUtils.isEmpty(goalCarbsSetting)?null:Integer.valueOf(goalCarbsSetting));

        return dto;
    }

    private String mapSetting(List<Setting> settings, String identifier) {
        return settings.stream().filter(s -> s.getName().equals(identifier)).max(Comparator.comparing(Setting::getDay)).orElse(new Setting()).getValue();
    }

}
