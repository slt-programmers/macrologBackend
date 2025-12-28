package slt.database;

import lombok.AllArgsConstructor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.Portion;

import jakarta.transaction.Transactional;

import java.util.List;

interface PortionCrudRepository extends CrudRepository<Portion, Long> {

    List<Portion> findByFoodIdAndId(final Long foodId, final Long portionId);

    List<Portion> findByFoodIdAndDescription(final Long foodId, final String description);

    List<Portion> findByFoodId(final Long foodId);

    void deleteByFoodIdIn(final List<Long> foodId);
}

@Repository
@AllArgsConstructor
public class PortionRepository {

    private PortionCrudRepository portionCrudRepository;

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

    public List<Portion> getAllPortions() {
        return (List<Portion>) portionCrudRepository.findAll();
    }

    @Transactional
    public void deleteAllForUser(final List<Long> foodIds) {
        if (foodIds.isEmpty()) {
            return;
        }
        portionCrudRepository.deleteByFoodIdIn(foodIds);
    }

}

