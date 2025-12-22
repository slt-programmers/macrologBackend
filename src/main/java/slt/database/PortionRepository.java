package slt.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.Portion;

import jakarta.transaction.Transactional;
import java.util.List;

interface PortionCrudRepository extends CrudRepository<Portion, Long> {

    List<Portion> findByFoodIdAndId(final Long foodId, final Long portionId);

    List<Portion> findByFoodIdAndDescription(final Long foodId, final String description);

    List<Portion> findByFoodId(final Long foodId);

    void deleteByFoodIdIn(List<Long> foodId);
}

@Repository
public class PortionRepository {

    @Autowired
    private PortionCrudRepository portionCrudRepository;


    public Portion savePortion(final Long foodId,final  Portion portion) {
        portion.setFoodId(foodId.intValue());
        return portionCrudRepository.save(portion);
    }

    public Portion getPortion(final Long foodId, final Long portionId) {
        List<Portion> queryResults = portionCrudRepository.findByFoodIdAndId(foodId, portionId);
        return queryResults.isEmpty() ? null : queryResults.getFirst();
    }

    public Portion getPortion(final Long foodId, final String description) {
        List<Portion> queryResults = portionCrudRepository.findByFoodIdAndDescription(foodId, description);
        return queryResults.isEmpty() ? null : queryResults.getFirst();
    }

    public List<Portion> getPortions(final Long foodId) {
        return portionCrudRepository.findByFoodId(foodId);
    }

    @Transactional
    public void deleteAllForUser(final List<Long> foodIds) {
        if (foodIds.isEmpty()) {
            return;
        }
        portionCrudRepository.deleteByFoodIdIn(foodIds);
    }

}

