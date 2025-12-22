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

interface ActivityCrudRepository extends CrudRepository<LogActivity, Long> {
    void deleteByUserIdAndId(final Long userId, Long activityId);

    void deleteByUserId(final Long userId);

    List<LogActivity> findByUserId(final Long userId);

    List<LogActivity> findByUserIdAndDay(final Long userId, Date date);

    Long countByUserIdAndSyncedWith(final Long userId, String strava);

    Optional<LogActivity> findByUserIdAndSyncedWithAndSyncedId(final Long userId, final String syncedWith, final Long syncedID);
}

@Repository
@Slf4j
public class ActivityRepository {

    @Autowired
    private ActivityCrudRepository activityCrudRepository;

    public LogActivity saveActivity(final Long userId, final LogActivity entry) {
        entry.setUserId(userId);
        return activityCrudRepository.save(entry);
    }

    @Transactional
    public void deleteLogActivity(final Long userId, final Long activtyId) {
        final Optional<LogActivity> byId = activityCrudRepository.findById(activtyId);
        if (byId.isPresent()) {
            final LogActivity logActivity = byId.get();
            if (StringUtils.isNotEmpty(logActivity.getSyncedWith())) {
                logActivity.setStatus("DELETED");
                activityCrudRepository.save(logActivity);
            } else {
                activityCrudRepository.deleteByUserIdAndId(userId, activtyId);
            }
        }
    }

    @Transactional
    public void deleteAllForUser(final Long userId) {
        activityCrudRepository.deleteByUserId(userId);
    }

    public List<LogActivity> getAllLogActivities(final Long userId) {
        return activityCrudRepository.findByUserId(userId);
    }

    public List<LogActivity> getAllLogActivities(final Long userId, final LocalDate date) {
        return activityCrudRepository.findByUserIdAndDay(userId, Date.valueOf(date));
    }

    public Long countByUserIdAndSyncedWith(final Long userId, final String strava) {
        return activityCrudRepository.countByUserIdAndSyncedWith(userId, strava);
    }

    public Optional<LogActivity> findByUserIdAndSyncIdAndSyncedWith(final Long userId, final String syncedWith, final Long syncedID) {
        return activityCrudRepository.findByUserIdAndSyncedWithAndSyncedId(userId, syncedWith, syncedID);
    }

}