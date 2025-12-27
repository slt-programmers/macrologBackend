package slt.database;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.Activity;

import jakarta.transaction.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

interface ActivityCrudRepository extends CrudRepository<Activity, Long> {

    void deleteByUserIdAndId(final Long userId, final Long activityId);

    void deleteByUserId(final Long userId);

    List<Activity> findByUserId(final Long userId);

    List<Activity> findByUserIdAndDay(final Long userId, final Date date);

    Long countByUserIdAndSyncedWith(final Long userId, final String strava);

    Optional<Activity> findByUserIdAndSyncedWithAndSyncedId(final Long userId, final String syncedWith, final Long syncedID);

}

@Slf4j
@Repository
@AllArgsConstructor
public class ActivityRepository {

    private ActivityCrudRepository activityCrudRepository;

    public Activity saveActivity(final Activity entry) {
        return activityCrudRepository.save(entry);
    }

    public Optional<Activity> findById(final Long id) {
        return activityCrudRepository.findById(id);
    }

    @Transactional
    public void deleteActivity(final Long userId, final Long activityId) {
        activityCrudRepository.deleteByUserIdAndId(userId, activityId);
    }

    @Transactional
    public void deleteAllForUser(final Long userId) {
        activityCrudRepository.deleteByUserId(userId);
    }

    public List<Activity> getAllActivities(final Long userId) {
        return activityCrudRepository.findByUserId(userId);
    }

    public List<Activity> getAllActivities(final Long userId, final LocalDate date) {
        return activityCrudRepository.findByUserIdAndDay(userId, Date.valueOf(date));
    }

    public Long countByUserIdAndSyncedWith(final Long userId, final String strava) {
        return activityCrudRepository.countByUserIdAndSyncedWith(userId, strava);
    }

    public Optional<Activity> findByUserIdAndSyncIdAndSyncedWith(final Long userId, final String syncedWith, final Long syncedID) {
        return activityCrudRepository.findByUserIdAndSyncedWithAndSyncedId(userId, syncedWith, syncedID);
    }

}