package slt.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.Portion;

import javax.transaction.Transactional;
import java.util.List;


interface PortionCrudRepository extends CrudRepository<Portion, Long> {

    List<Portion> findByFoodIdAndId(Integer foodId, Long portionId);

    List<Portion> findByFoodIdAndDescription(Integer foodId, String description);

    List<Portion> findByFoodId(Integer foodId);

    void deleteByFoodIdIn(List<Integer> foodId);
}

@Repository
public class PortionRepository {

    @Autowired
    private PortionCrudRepository portionCrudRepository;


    public Portion savePortion(Long foodId, Portion portion) {
        portion.setFoodId(foodId.intValue());
        return portionCrudRepository.save(portion);
    }

    public Portion getPortion(Long foodId, Long portionId) {
        List<Portion> queryResults = portionCrudRepository.findByFoodIdAndId(foodId.intValue(), portionId);
        return queryResults.isEmpty() ? null : queryResults.get(0);
    }

    public Portion getPortion(Long foodId, String description) {
        List<Portion> queryResults = portionCrudRepository.findByFoodIdAndDescription(foodId.intValue(), description);
        return queryResults.isEmpty() ? null : queryResults.get(0);
    }

    public List<Portion> getPortions(Long foodId) {
        return portionCrudRepository.findByFoodId(foodId.intValue());
    }

    @Transactional
    public void deleteAllForUser(List<Integer> foodIds) {
        if (foodIds.isEmpty()) {
            return;
        }
        portionCrudRepository.deleteByFoodIdIn(foodIds);
    }

}

