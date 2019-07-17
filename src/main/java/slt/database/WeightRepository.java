package slt.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.Weight;

import javax.transaction.Transactional;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

interface WeightCrudRepository extends CrudRepository<Weight, Integer> {

    List<Weight> findByUserId(Integer userId);
    List<Weight> findByUserIdAndDay(Integer userId, Date day);

    void deleteByUserId(Integer userId);
    void deleteByIdAndUserId(Integer weightId, Integer userId);
}


@Repository
public class WeightRepository {

    @Autowired
    private WeightCrudRepository weightCrudRepository;

    public Weight insertWeight(Integer userId, Weight newEntry) {
        newEntry.setUserId(userId);
        return weightCrudRepository.save(newEntry);
    }

    public Weight updateWeight(Integer userId, Weight entry) {
        Optional<Weight> byId = weightCrudRepository.findById(entry.getId());
        if (!byId.isPresent()){
            throw new IllegalArgumentException("Update on non existing entry");
        }
        Weight weight = byId.get();
        weight.setWeight(entry.getWeight());
        weight.setRemark(entry.getRemark());
        weight.setDay(entry.getDay());
        entry.setUserId(userId);
        return weightCrudRepository.save(weight);
    }

    @Transactional
    public void deleteWeightByIdAndUserId(Long entry, Integer userId) {
        weightCrudRepository.deleteByIdAndUserId(entry.intValue(), userId);
    }

    @Transactional
    public void deleteAllForUser(Integer userId) {
        weightCrudRepository.deleteByUserId(userId);
    }

    public List<Weight> getWeightEntryForDay(Integer userId, Date day) {
        List<slt.database.entities.Weight> byUserId = weightCrudRepository.findByUserIdAndDay(userId, day);
        return byUserId;
    }

    public List<Weight> getAllWeightEntries(Integer userId) {
        List<slt.database.entities.Weight> byUserId = weightCrudRepository.findByUserId(userId);
        return byUserId;
    }

}