package slt.database;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import slt.database.entities.Setting;

import jakarta.transaction.Transactional;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

interface SettingsCrudRepository extends CrudRepository<Setting, Long> {

    List<Setting> findByUserIdOrderByDayDesc(final Long userId);

    List<Setting> findByUserIdAndNameOrderByDayDesc(final Long userId, final String name);

    void deleteByUserId(final Long userId);

    void deleteAllByUserIdAndName(final Long userId, final String name);

    @Query("SELECT s FROM Setting s WHERE s.userId = :userId AND s.name = :name AND s.day <= :day ORDER BY s.day DESC")
    List<Setting> findByUserIdAndNameWithDayBeforeDay(@Param("userId") final Long userId, @Param("name") final String name, @Param("day") final java.util.Date day);

    @Query("SELECT s FROM Setting s WHERE s.userId = :userId AND s.name = :name AND s.day >= :day ORDER BY s.day ASC")
    List<Setting> findByUserIdAndNameWithDayAfterDay(@Param("userId") final Long userId, @Param("name") final String name, @Param("day") final java.util.Date day);

    Optional<Setting> findByNameAndValue(final String name, final String value);
}

@Slf4j
@Repository
@AllArgsConstructor
public class SettingsRepository {

    private SettingsCrudRepository settingsCrudRepository;

    public void putSetting(final Setting setting) {
        final var optionalSetting = getValidSetting(setting.getUserId(), setting.getName(), setting.getDay());
        if (optionalSetting.isEmpty()) { // geen records
            log.debug("Insert");
            // new settings cant have id's. clear it:
            setting.setId(null);
            saveSetting(setting.getUserId(), setting);
        } else {
            log.debug("Update");
            final var currentSetting = optionalSetting.get();
            boolean settingSameDay = currentSetting.getDay().toLocalDate().equals(setting.getDay().toLocalDate());
            if (settingSameDay) {
                currentSetting.setValue(setting.getValue());
                settingsCrudRepository.save(currentSetting);
            } else {
                saveSetting(setting.getUserId(), setting);
            }
        }
    }

    @Transactional
    public void deleteAllForUser(final Long userId) {
        settingsCrudRepository.deleteByUserId(userId);
    }

    @Transactional
    public void deleteAllForUser(final Long userId, String name) {
        settingsCrudRepository.deleteAllByUserIdAndName(userId, name);
    }

    public Optional<Setting> getLatestSetting(final Long userId, final String setting) {
        final var byUserIdAndName = settingsCrudRepository.findByUserIdAndNameOrderByDayDesc(userId, setting);
        log.debug("Number of hits for setting {}: {}", setting, byUserIdAndName.size());
        return byUserIdAndName.stream().findFirst();
    }

    public Optional<Setting> getValidSetting(final Long userId, final String setting, final Date date) {
        final var byUserIdAndNameWithDayBeforeDay = settingsCrudRepository.findByUserIdAndNameWithDayBeforeDay(userId, setting, date);
        log.debug("Number of hits for setting on or before date {}: {}", setting, byUserIdAndNameWithDayBeforeDay.size());

        if (byUserIdAndNameWithDayBeforeDay.isEmpty()) {
            final var byUserIdAndNameWithDayAfterDay = settingsCrudRepository.findByUserIdAndNameWithDayAfterDay(userId, setting, date);
            log.debug("Number of hits for setting on or after date {}: {}", setting, byUserIdAndNameWithDayAfterDay.size());
            return byUserIdAndNameWithDayAfterDay.stream().findFirst();
        } else {
            return byUserIdAndNameWithDayBeforeDay.stream().findFirst();
        }
    }

    public List<Setting> getAllSettings(final Long userId) {
        return settingsCrudRepository.findByUserIdOrderByDayDesc(userId);
    }

    public void saveSetting(final Long userId, final Setting settingDomain) {
        settingDomain.setUserId(userId);
        settingsCrudRepository.save(settingDomain);
    }

    public Optional<Setting> findByKeyValue(final String name, final String value) {
        return settingsCrudRepository.findByNameAndValue(name, value);
    }
}

