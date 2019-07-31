package slt.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import slt.database.entities.Setting;

import javax.transaction.Transactional;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

interface SettingsCrudRepository extends CrudRepository<Setting, Integer> {

    List<Setting> findByUserIdOrderByDayDesc(Integer userId);

    List<Setting> findByUserIdAndNameOrderByDayDesc(Integer userId, String name);

    void deleteByUserId(Integer userId);

    void deleteAllByUserIdAndName(Integer userId, String name);

    @Query("select s from Setting s where s.userId = :userId and s.name = :name and s.day <= :day")
    List<Setting> findByUserIdAndNameWithDayBeforeDay(@Param("userId") Integer userId, @Param("name") String name, @Param("day") java.util.Date day);
}

@Repository
@Slf4j
public class SettingsRepository {

    @Autowired
    private SettingsCrudRepository settingsCrudRepository;

    public void putSetting(Integer userId, String setting, String value, Date date) {
        date = date == null ? Date.valueOf(LocalDate.now()):date;
        Setting currentSetting = getValidSetting(userId, setting, date);
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


    public void putSetting(Integer userId, Setting setting) {
        setting.setUserId(userId);
        Setting currentSetting = getValidSetting(userId, setting.getName(), setting.getDay());
        if (currentSetting == null) { // geen records
            log.debug("Insert");
            saveSetting(userId, setting);
        } else {
            log.debug("Update");
            boolean settingSameDay = currentSetting.getDay().toLocalDate().equals(setting.getDay().toLocalDate());
            if (settingSameDay) {
                currentSetting.setValue(setting.getValue());
                settingsCrudRepository.save(currentSetting);
            } else {
                saveSetting(userId, setting);
            }
        }
    }

    public Setting insertSetting(Integer userId, String setting, String value, Date date) {
        Setting newSetting = Setting.builder().day(date).name(setting).userId(userId).value(value).build();
        return settingsCrudRepository.save(newSetting);
    }

    @Transactional
    public void deleteAllForUser(Integer userId) {
        settingsCrudRepository.deleteByUserId(userId);
    }

    @Transactional
    public void deleteAllForUser(Integer userId, String name) {
        settingsCrudRepository.deleteAllByUserIdAndName(userId,name);
    }

    public Setting getLatestSetting(Integer userId, String setting) {
        List<Setting> byUserIdAndName = settingsCrudRepository.findByUserIdAndNameOrderByDayDesc(userId, setting);
        log.debug("Number of hits for setting {} :{}", setting, byUserIdAndName.size());
        return byUserIdAndName.isEmpty() ? null : byUserIdAndName.get(0);
    }

    public Setting getValidSetting(Integer userId, String setting, Date date) {
        List<Setting> byUserIdAndNameWithDayBeforeDay = settingsCrudRepository.findByUserIdAndNameWithDayBeforeDay(userId, setting, date);
        log.debug("Number of hits for setting on date {} :{}", setting, byUserIdAndNameWithDayBeforeDay.size());
        return byUserIdAndNameWithDayBeforeDay.isEmpty() ? null : byUserIdAndNameWithDayBeforeDay.get(0);
    }

    public List<Setting> getAllSettings(Integer userId) {
        return settingsCrudRepository.findByUserIdOrderByDayDesc(userId);
    }

    public Setting saveSetting(Integer userId, Setting settingDomain) {
        settingDomain.setUserId(userId);
        return settingsCrudRepository.save(settingDomain);
    }
}

