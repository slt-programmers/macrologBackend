package slt.service;

import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import slt.database.SettingsRepository;
import slt.database.WeightRepository;
import slt.database.entities.Setting;
import slt.dto.SettingDto;
import slt.dto.UserSettingsDto;
import slt.dto.WeightDto;
import slt.exceptions.NotFoundException;
import slt.mapper.SettingsMapper;

import java.sql.Date;
import java.time.LocalDate;

@Service
@AllArgsConstructor
public class SettingsService {

    private SettingsRepository settingsRepository;
    private WeightRepository weightRepository;
    private WeightService weightService;

    private final SettingsMapper settingsMapper = SettingsMapper.INSTANCE;

    private static final String WEIGHT = "weight";

    public UserSettingsDto getUserSettingsDto(final Long userId) {
        final var settings = settingsRepository.getAllSettings(userId);
        final var currentWeight = weightRepository.getLatestWeight(userId);
        final var userSettingsDto = settingsMapper.mapToUserSettingsDto(settings);
        currentWeight.ifPresent(value -> userSettingsDto.setCurrentWeight(value.getWeight()));
        return userSettingsDto;
    }

    public String getSetting(final Long userId, final String name, final String date) {
        Setting setting;
        if (StringUtils.isEmpty(date)) {
            setting = settingsRepository.getLatestSetting(userId, name);
        } else {
            setting = settingsRepository.getValidSetting(userId, name, Date.valueOf(date));
        }
        if (setting == null) {
            throw new NotFoundException("Setting [" + name + "] for userId [" + userId + "] not found.");
        }
        return setting.getValue();
    }

    public void putSetting(final Long userId, final SettingDto settingDto) {
        final var day = settingDto.getDay() == null ? LocalDate.now() : settingDto.getDay();
        if (WEIGHT.equals(settingDto.getName())) {
            final var weightDto = WeightDto.builder()
                    .weight(Double.valueOf(settingDto.getValue()))
                    .day(day)
                    .build();
            weightService.saveWeight(userId, weightDto);
        } else {
            settingDto.setDay(day);
            final var setting = settingsMapper.map(settingDto, userId);
            settingsRepository.putSetting(setting);
        }
    }

}
