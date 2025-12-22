package slt.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import slt.database.entities.LogEntry;

import jakarta.transaction.Transactional;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

interface LogEntryCrudRepository extends CrudRepository<LogEntry, Long> {
    void deleteByUserIdAndId(final Long userId, Long logEntryId);

    void deleteByUserId(final Long userId);

    List<LogEntry> findByUserId(final Long userId);

    List<LogEntry> findByUserIdAndDay(final Long userId, Date date);

    List<LogEntry> findByUserIdAndDayAndMeal(final Long userId, Date date, String meal);

    @Query("select l from LogEntry l where l.userId = :userId and l.day >= :begin and l.day <= :end")
    List<LogEntry> findByUserIdWithDayAfterAndDayBefore(@Param("userId") final Long userId, @Param("begin") Date begin, @Param("end") Date end);
}

@Repository
@Slf4j
public class LogEntryRepository {

    @Autowired
    private LogEntryCrudRepository logEntryCrudRepository;

    @Transactional
    public void saveLogEntry(final Long userId, final LogEntry entry) {
        entry.setUserId(userId);
        logEntryCrudRepository.save(entry);
    }
    
    @Transactional
    public void deleteLogEntry(final Long userId, final Long logEntryId) {
        logEntryCrudRepository.deleteByUserIdAndId(userId, logEntryId);
    }

    @Transactional
    public void deleteAllForUser(final Long userId) {
        logEntryCrudRepository.deleteByUserId(userId);
    }

    public List<LogEntry> getAllLogEntries(final Long userId) {
        return logEntryCrudRepository.findByUserId(userId);
    }

    public List<LogEntry> getAllLogEntries(final Long userId, final LocalDate date) {
        log.debug("Getting entries for " + date);
        return logEntryCrudRepository.findByUserIdAndDay(userId, Date.valueOf(date));
    }

    public List<LogEntry> getAllLogEntries(final Long userId, final LocalDate date, final String meal) {
        log.debug("Getting entries for " + date + " and " + meal);
        return logEntryCrudRepository.findByUserIdAndDayAndMeal(userId, Date.valueOf(date), meal);
    }
    public List<LogEntry> getAllLogEntries(final Long userId, final Date begin, final Date end) {
        log.debug("Getting entries for period " + begin + " - " + end);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        log.debug("BETWEEN " + sdf.format(begin) + " AND " + sdf.format(end));
        return logEntryCrudRepository.findByUserIdWithDayAfterAndDayBefore(userId,begin,end);
    }
}