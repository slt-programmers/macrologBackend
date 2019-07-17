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
import java.util.List;

interface SettingsCrudRepository extends CrudRepository<Setting,Integer> {

    List<Setting> findByUserIdOrderByDayDesc(Integer userId);
    List<Setting> findByUserIdAndNameOrderByDayDesc(Integer userId, String name);
    void deleteByUserId(Integer userId);

    @Query("select s from Setting s where s.userId = :userId and s.name = :name and s.day <= :day")
    List<Setting> findByUserIdAndNameWithDayBeforeDay(@Param("userId") Integer userId, @Param("name") String name, @Param("day") java.util.Date day);
}

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
        log.debug("Number of hits for {} :{}", setting, byUserIdAndName.size());
        return byUserIdAndName.isEmpty() ? null : byUserIdAndName.get(0);
    }

    public Setting getValidSettingOLD(Integer userId, String setting, Date date) {

        List<slt.database.entities.Setting> byUserIdAndNameWithDayBeforeDay = settingsCrudRepository.findByUserIdAndNameWithDayBeforeDay(userId, setting, date);
        log.debug("Number of hits for {} :{}", setting, byUserIdAndNameWithDayBeforeDay.size());
        return byUserIdAndNameWithDayBeforeDay.isEmpty() ? null : byUserIdAndNameWithDayBeforeDay.get(0);
    }

    public Setting getValidSetting(Integer userId, String setting, Date date) {
        List<slt.database.entities.Setting> byUserIdAndNameWithDayBeforeDay = settingsCrudRepository.findByUserIdAndNameWithDayBeforeDay(userId, setting, date);
        log.debug("Number of hits for {} :{}", setting, byUserIdAndNameWithDayBeforeDay.size());
        return byUserIdAndNameWithDayBeforeDay.isEmpty() ? null : byUserIdAndNameWithDayBeforeDay.get(0);
    }

    public List<Setting> getAllSettings(Integer userId) {
        List<slt.database.entities.Setting> byUserId = settingsCrudRepository.findByUserIdOrderByDayDesc(userId);
        return byUserId;
    }

}

