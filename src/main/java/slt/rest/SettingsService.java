package slt.rest;

import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.connectivity.strava.StravaActivityService;
import slt.database.SettingsRepository;
import slt.database.WeightRepository;
import slt.database.entities.Setting;
import slt.database.entities.Weight;
import slt.dto.*;
import slt.mapper.SettingMapper;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.util.LocalDateParser;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/settings")
public class SettingsService {

    private SettingsRepository settingsRepo;
    private WeightRepository weightRepo;
    private WeightController weightController;

    private StravaActivityService stravaActivityService;

    private final SettingMapper settingMapper = SettingMapper.INSTANCE;

    @PutMapping
    public ResponseEntity<Void> putSetting(@RequestBody final SettingDto settingDto) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        if ("weight".equals(settingDto.getName())) {
            weightController.postWeight(
                    new WeightDto(null,
                            Double.valueOf(settingDto.getValue()),
                            settingDto.getDay() == null ? LocalDate.now() : settingDto.getDay().toLocalDate(),
                            null));
        }
        if (settingDto.getDay() == null) {
            settingDto.setDay(Date.valueOf(LocalDate.now()));
        }
        final var setting = settingMapper.map(settingDto, userInfo.getUserId());
        settingsRepo.putSetting(userInfo.getUserId(), setting);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping(path = "/connectivity/{name}")
    public ResponseEntity<SyncedAccount> getConnectivitySetting(@PathVariable("name") final String name) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var syncedAccount = stravaActivityService.getStravaConnectivity(userInfo.getUserId());
        if (syncedAccount == null) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.ok(syncedAccount);
        }
    }

    @PostMapping(path = "/connectivity/{name}")
    public ResponseEntity<SyncedAccount> storeConnectivitySetting(@RequestBody final SettingDto code) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        final SyncedAccount syncedAccount = stravaActivityService.registerStravaConnectivity(userInfo.getUserId(), code.getValue());
        return ResponseEntity.ok(syncedAccount);
    }

    @DeleteMapping(path = "/connectivity/{name}")
    public ResponseEntity<Void> disConnectConnectivitySetting(@PathVariable("name") String name) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        stravaActivityService.unRegisterStrava(userInfo.getUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/user")
    public ResponseEntity<UserSettingsDto> getUserSetting() {
        final var userSettingsDto = getUserSettingsDto();
        return ResponseEntity.ok(userSettingsDto);
    }

    @GetMapping(path = "/{name}")
    public ResponseEntity<String> getSetting(@PathVariable("name") final String name,
                                             @RequestParam(value = "date", required = false) final String toDate) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
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
        if (setting == null) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.ok(setting.getValue());
        }
    }

    private UserSettingsDto getUserSettingsDto() {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<Setting> settings = settingsRepo.getAllSettings(userInfo.getUserId());
        List<Weight> weight = weightRepo.getAllWeightEntries(userInfo.getUserId());
        Weight currentWeight = weight.stream().max(Comparator.comparing(Weight::getDay)).orElse(null);
        UserSettingsDto userSettingsDto = mapToUserSettingsDto(settings);
        if (currentWeight != null) {
            userSettingsDto.setCurrentWeight(currentWeight.getWeight());
        }
        return userSettingsDto;
    }

    private UserSettingsDto mapToUserSettingsDto(List<Setting> settings) {
        UserSettingsDto dto = new UserSettingsDto();
        dto.setName(mapSetting(settings, "name"));
        dto.setGender(mapSetting(settings, "gender"));
        String ageSetting = mapSetting(settings, "age");
        dto.setAge(StringUtils.isEmpty(ageSetting) ? null : Integer.valueOf(ageSetting));
        String birthdaySetting = mapSetting(settings, "birthday");
        dto.setBirthday(StringUtils.isEmpty(birthdaySetting) ? null : LocalDateParser.parse(birthdaySetting));
        String heightSetting = mapSetting(settings, "height");
        dto.setHeight(StringUtils.isEmpty(heightSetting) ? null : Integer.valueOf(heightSetting));
        String activitySetting = mapSetting(settings, "activity");
        dto.setActivity(StringUtils.isEmpty(activitySetting) ? null : Double.valueOf(activitySetting));

        String goalProteinSetting = mapSetting(settings, "goalProtein");
        dto.setGoalProtein(StringUtils.isEmpty(goalProteinSetting) ? null : Integer.valueOf(goalProteinSetting));
        String goalFatSetting = mapSetting(settings, "goalFat");
        dto.setGoalFat(StringUtils.isEmpty(goalFatSetting) ? null : Integer.valueOf(goalFatSetting));
        String goalCarbsSetting = mapSetting(settings, "goalCarbs");
        dto.setGoalCarbs(StringUtils.isEmpty(goalCarbsSetting) ? null : Integer.valueOf(goalCarbsSetting));

        return dto;
    }

    private String mapSetting(final List<Setting> settings, final String identifier) {
        return settings.stream()
                .filter(s -> s.getName().equals(identifier))
                .max(Comparator.comparing(Setting::getDay))
                .orElse(new Setting()).getValue();
    }

}
