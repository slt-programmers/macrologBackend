package slt.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.LogActivity;

import javax.transaction.Transactional;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

interface LogActivityCrudRepository extends CrudRepository<LogActivity, Integer> {
    void deleteByUserIdAndId(Integer userId, Long activtyId);

    void deleteByUserId(Integer userId);

    List<LogActivity> findByUserId(Integer userId);

    List<LogActivity> findByUserIdAndDay(Integer userId, Date date);
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
        logActivityCrudRepository.deleteByUserIdAndId(userId, activtyId);
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

}