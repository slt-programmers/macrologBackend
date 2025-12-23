package slt.database;

import lombok.AllArgsConstructor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.Weight;

import jakarta.transaction.Transactional;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

interface WeightCrudRepository extends CrudRepository<Weight, Long> {

    List<Weight> findByUserIdOrderByDayDesc(final Long userId);

    List<Weight> findByUserIdAndDay(final Long userId, final Date day);

    void deleteByUserId(final Long userId);

    void deleteByIdAndUserId(final Long weightId, final Long userId);

}

@Repository
@AllArgsConstructor
public class WeightRepository {

    private WeightCrudRepository weightCrudRepository;

    public Weight saveWeight(final Weight weight) {
        return weightCrudRepository.save(weight);
    }

    @Transactional
    public void deleteWeightByIdAndUserId(final Long entry, final Long userId) {
        weightCrudRepository.deleteByIdAndUserId(entry, userId);
    }

    @Transactional
    public void deleteAllForUser(final Long userId) {
        weightCrudRepository.deleteByUserId(userId);
    }

    public List<Weight> getWeightEntryForDay(final Long userId, final Date day) {
        return weightCrudRepository.findByUserIdAndDay(userId, day);
    }

    public List<Weight> getAllWeightEntries(final Long userId) {
        return weightCrudRepository.findByUserIdOrderByDayDesc(userId);
    }

    public Optional<Weight> getLatestWeight(final Long userId) {
        return weightCrudRepository.findByUserIdOrderByDayDesc(userId).stream().findFirst();
    }
}