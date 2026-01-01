package slt.database;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import slt.database.entities.Entry;

import jakarta.transaction.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

interface EntryCrudRepository extends CrudRepository<Entry, Long> {
    void deleteByUserIdAndId(final Long userId, final Long entryId);

    void deleteByUserId(final Long userId);

    List<Entry> findByUserId(final Long userId);

    List<Entry> findByUserIdAndDay(final Long userId, final Date date);

    List<Entry> findByUserIdAndDayAndMeal(final Long userId, final Date date, final String meal);

    @Query("select l from Entry l where l.userId = :userId and l.day >= :begin and l.day <= :end")
    List<Entry> findByUserIdWithDayAfterAndDayBefore(@Param("userId") final Long userId, @Param("begin") final Date begin, @Param("end") final Date end);
}

@Slf4j
@Repository
@AllArgsConstructor
public class EntryRepository {

    private EntryCrudRepository entryCrudRepository;

    @Transactional
    public void saveEntry(final Entry entry) {
        entryCrudRepository.save(entry);
    }

    @Transactional
    public void deleteEntry(final Long userId, final Long entryId) {
        entryCrudRepository.deleteByUserIdAndId(userId, entryId);
    }

    @Transactional
    public void deleteAllForUser(final Long userId) {
        entryCrudRepository.deleteByUserId(userId);
    }

    public List<Entry> getAllEntries(final Long userId) {
        return entryCrudRepository.findByUserId(userId);
    }

    public List<Entry> getAllEntries(final Long userId, final LocalDate date) {
        log.debug("Getting entries for {}", date);
        return entryCrudRepository.findByUserIdAndDay(userId, Date.valueOf(date));
    }

    public List<Entry> getAllEntries(final Long userId, final LocalDate date, final String meal) {
        log.debug("Getting entries for {} and {}", date, meal);
        return entryCrudRepository.findByUserIdAndDayAndMeal(userId, Date.valueOf(date), meal);
    }

    public List<Entry> getAllEntries(final Long userId, final LocalDate begin, final LocalDate end) {
        log.debug("Getting entries for period {} - {}", begin, end);
        return entryCrudRepository.findByUserIdWithDayAfterAndDayBefore(userId, Date.valueOf(begin), Date.valueOf(end));
    }
}