package slt.mapper;

import org.apache.commons.lang.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Setting;
import slt.dto.SettingDto;
import slt.dto.UserSettingsDto;
import slt.util.LocalDateParser;

import java.util.Comparator;
import java.util.List;

@Mapper
public interface SettingsMapper {

    SettingsMapper INSTANCE = Mappers.getMapper(SettingsMapper.class);

    Setting map(final SettingDto settingDto, final Long userId);

    default UserSettingsDto mapToUserSettingsDto(final List<Setting> settings) {
        final var dto = new UserSettingsDto();

        dto.setName(mapSetting(settings, "name"));
        dto.setGender(mapSetting(settings, "gender"));
        final var ageSetting = mapSetting(settings, "age");
        dto.setAge(StringUtils.isEmpty(ageSetting) ? null : Integer.valueOf(ageSetting));
        final var birthdaySetting = mapSetting(settings, "birthday");
        dto.setBirthday(StringUtils.isEmpty(birthdaySetting) ? null : LocalDateParser.parse(birthdaySetting));
        final var heightSetting = mapSetting(settings, "height");
        dto.setHeight(StringUtils.isEmpty(heightSetting) ? null : Integer.valueOf(heightSetting));
        final var activitySetting = mapSetting(settings, "activity");
        dto.setActivity(StringUtils.isEmpty(activitySetting) ? null : Double.valueOf(activitySetting));

        final var goalProteinSetting = mapSetting(settings, "goalProtein");
        dto.setGoalProtein(StringUtils.isEmpty(goalProteinSetting) ? null : Integer.valueOf(goalProteinSetting));
        final var goalFatSetting = mapSetting(settings, "goalFat");
        dto.setGoalFat(StringUtils.isEmpty(goalFatSetting) ? null : Integer.valueOf(goalFatSetting));
        final var goalCarbsSetting = mapSetting(settings, "goalCarbs");
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
