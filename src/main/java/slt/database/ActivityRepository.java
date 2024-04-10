package slt.database;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.LogActivity;

import jakarta.transaction.Transactional;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

interface LogActivityCrudRepository extends CrudRepository<LogActivity, Long> {
    void deleteByUserIdAndId(Integer userId, Long activityId);

    void deleteByUserId(Integer userId);

    List<LogActivity> findByUserId(Integer userId);

    List<LogActivity> findByUserIdAndDay(Integer userId, Date date);

    Long countByUserIdAndSyncedWith(Integer userId, String strava);

    Optional<LogActivity> findByUserIdAndSyncedWithAndSyncedId(Integer userId, String syncedWith, Long syncedID);
}

@Repository
@Slf4j
public class ActivityRepository {

    @Autowired
    private LogActivityCrudRepository logActivityCrudRepository;

    public LogActivity saveActivity(Integer userId, LogActivity entry) {
        entry.setUserId(userId);
        return logActivityCrudRepository.save(entry);
    }

    @Transactional
    public void deleteLogActivity(Integer userId, Long activtyId) {
        final Optional<LogActivity> byId = logActivityCrudRepository.findById(activtyId);
        if (byId.isPresent()) {
            final LogActivity logActivity = byId.get();
            if (StringUtils.isNotEmpty(logActivity.getSyncedWith())) {
                logActivity.setStatus("DELETED");
                logActivityCrudRepository.save(logActivity);
            } else {
                logActivityCrudRepository.deleteByUserIdAndId(userId, activtyId);
            }
        }
    }

    @Transactional
    public void deleteAllForUser(Integer userId) {
        logActivityCrudRepository.deleteByUserId(userId);
    }

    public List<LogActivity> getAllLogActivities(Integer userId) {
        log.debug("Getting entries for " + userId);
        return logActivityCrudRepository.findByUserId(userId);
    }

    public List<LogActivity> getAllLogActivities(Integer userId, LocalDate date) {
        log.debug("Getting entries for " + date);
        return logActivityCrudRepository.findByUserIdAndDay(userId, Date.valueOf(date));
    }

    public Long countByUserIdAndSyncedWith(Integer userId, String strava) {
        return logActivityCrudRepository.countByUserIdAndSyncedWith(userId, strava);
    }

    public Optional<LogActivity> findByUserIdAndSyncIdAndSyncedWith(Integer userId, String syncedWith, Long syncedID) {
        return logActivityCrudRepository.findByUserIdAndSyncedWithAndSyncedId(userId, syncedWith,syncedID);
    }

}