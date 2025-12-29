package slt.mapper;

import org.apache.commons.lang.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Setting;
import slt.dto.SettingDto;
import slt.dto.UserSettingsDto;
import slt.util.LocalDateParser;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Mapper
public interface SettingsMapper {

    SettingsMapper INSTANCE = Mappers.getMapper(SettingsMapper.class);

    @Mapping(source = "settingDto.day", target = "day", qualifiedByName = "dayDefault")
    Setting map(final SettingDto settingDto, final Long userId);

    @Named("dayDefault")
    default Date dayDefault(final LocalDate localDate) {
        if (localDate == null) return Date.valueOf(LocalDate.now());
        return Date.valueOf(localDate);
    }

    SettingDto map(final Setting setting);

    default UserSettingsDto mapToUserSettingsDto(final List<Setting> settings) {
        final var dto = new UserSettingsDto();

        dto.setName(getLatestSetting(settings, "name"));
        dto.setGender(getLatestSetting(settings, "gender"));
        final var ageSetting = getLatestSetting(settings, "age");
        dto.setAge(StringUtils.isEmpty(ageSetting) ? null : Integer.valueOf(ageSetting));
        final var birthdaySetting = getLatestSetting(settings, "birthday");
        dto.setBirthday(StringUtils.isEmpty(birthdaySetting) ? null : LocalDateParser.parse(birthdaySetting));
        final var heightSetting = getLatestSetting(settings, "height");
        dto.setHeight(StringUtils.isEmpty(heightSetting) ? null : Integer.valueOf(heightSetting));
        final var activitySetting = getLatestSetting(settings, "activity");
        dto.setActivity(StringUtils.isEmpty(activitySetting) ? null : Double.valueOf(activitySetting));

        final var goalProteinSetting = getLatestSetting(settings, "goalProtein");
        dto.setGoalProtein(StringUtils.isEmpty(goalProteinSetting) ? null : Integer.valueOf(goalProteinSetting));
        final var goalFatSetting = getLatestSetting(settings, "goalFat");
        dto.setGoalFat(StringUtils.isEmpty(goalFatSetting) ? null : Integer.valueOf(goalFatSetting));
        final var goalCarbsSetting = getLatestSetting(settings, "goalCarbs");
        dto.setGoalCarbs(StringUtils.isEmpty(goalCarbsSetting) ? null : Integer.valueOf(goalCarbsSetting));

        return dto;
    }

    private String getLatestSetting(final List<Setting> settings, final String identifier) {
        return settings.stream()
                .filter(s -> s.getName().equals(identifier))
                .max(Comparator.comparing(Setting::getDay))
                .orElse(new Setting()).getValue();
    }
}
