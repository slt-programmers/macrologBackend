package slt.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import slt.database.model.Setting;

import javax.transaction.Transactional;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class SettingsRepository {

    @Autowired
    private SettingsCrudRepository settingsCrudRepository;

    public void putSetting(Integer userId, String setting, String value, Date date) {
        slt.database.entities.Setting currentSetting = getValidSetting(userId, setting, date);
        if (currentSetting == null) { // geen records
            log.debug("Insert");
            insertSetting(userId, setting, value, date).getId();
        } else {
            log.debug("Update");
            boolean settingSameDay = currentSetting.getDay().toLocalDate().equals(date.toLocalDate());
            if (settingSameDay) {
                currentSetting.setValue(value);
                settingsCrudRepository.save(currentSetting);
            } else {
                insertSetting(userId, setting, value, date).getId();
            }
        }
    }

    public slt.database.entities.Setting insertSetting(Integer userId, String setting, String value, Date date) {
        slt.database.entities.Setting newSetting = slt.database.entities.Setting.builder().day(date).name(setting).userId(userId).value(value).build();
        slt.database.entities.Setting savedSetting = settingsCrudRepository.save(newSetting);
        return savedSetting;
    }

    @Transactional
    public void deleteAllForUser(Integer userId) {
        settingsCrudRepository.deleteByUserId(userId);
    }

    public Setting getLatestSetting(Integer userId, String setting) {
        List<slt.database.entities.Setting> byUserIdAndName = settingsCrudRepository.findByUserIdAndNameOrderByDayDesc(userId, setting);
        List<Setting> queryResults = transformToOldSetting(byUserIdAndName);
        log.debug("Number of hits for {} :{}", setting, queryResults.size());
        return queryResults.isEmpty() ? null : queryResults.get(0);
    }

    public Setting getValidSettingOLD(Integer userId, String setting, Date date) {

        List<slt.database.entities.Setting> byUserIdAndNameWithDayBeforeDay = settingsCrudRepository.findByUserIdAndNameWithDayBeforeDay(userId, setting, date);
        List<Setting> queryResults = transformToOldSetting(byUserIdAndNameWithDayBeforeDay);
        log.debug("Number of hits for {} :{}", setting, queryResults.size());
        return queryResults.isEmpty() ? null : queryResults.get(0);
    }

    public slt.database.entities.Setting getValidSetting(Integer userId, String setting, Date date) {

        List<slt.database.entities.Setting> byUserIdAndNameWithDayBeforeDay = settingsCrudRepository.findByUserIdAndNameWithDayBeforeDay(userId, setting, date);
        log.debug("Number of hits for {} :{}", setting, byUserIdAndNameWithDayBeforeDay.size());
        return byUserIdAndNameWithDayBeforeDay.isEmpty() ? null : byUserIdAndNameWithDayBeforeDay.get(0);
    }

    public List<Setting> getAllSettings(Integer userId) {
        List<slt.database.entities.Setting> byUserId = settingsCrudRepository.findByUserIdOrderByDayDesc(userId);
        return transformToOldSetting(byUserId);
    }

    private List<Setting> transformToOldSetting(List<slt.database.entities.Setting> newSetting) {
        return newSetting.stream().map(s -> transformToOldSetting(s)).collect(Collectors.toList());

    }

    private Setting transformToOldSetting(slt.database.entities.Setting newSetting) {
        return Setting.builder().name(newSetting.getName()).value(newSetting.getValue()).day(newSetting.getDay()).id(newSetting.getId().longValue()).build();
    }

}

