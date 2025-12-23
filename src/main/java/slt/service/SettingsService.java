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
import slt.exceptions.InvalidDateException;
import slt.exceptions.NotFoundException;
import slt.mapper.SettingsMapper;
import slt.rest.WeightController;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

@Service
@AllArgsConstructor
public class SettingsService {

    private SettingsRepository settingsRepository;
    private WeightRepository weightRepository;
    private WeightController weightController;

    private final SettingsMapper settingsMapper = SettingsMapper.INSTANCE;

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
        if ("weight".equals(settingDto.getName())) {
            final var weightDto = WeightDto.builder()
                    .weight(Double.valueOf(settingDto.getValue()))
                    .day(day)
                    .build();
            // TODO move to a service
            weightController.postWeight(weightDto);
        } else {
            settingDto.setDay(day);
            final var setting = settingsMapper.map(settingDto, userId);
            settingsRepository.putSetting(setting);
        }
    }

    public void validateDateFormat(final String date) {
        if (date != null) {
            try {
                final var dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setLenient(false);
                dateFormat.parse(date);
            } catch (ParseException pe) {
                throw new InvalidDateException("Date format is not valid.");
            }
        }
    }
}
