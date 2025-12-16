package slt.database;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import slt.database.entities.Mealplan;

import java.util.List;

interface MealplanCrudRepository extends CrudRepository<Mealplan, Long> {

    void deleteByUserIdAndId(Integer userId, Long dishId);

    void deleteByUserId(Integer userId);

    List<Mealplan> findByUserId(Integer userId);

}

@Service
@AllArgsConstructor
public class MealplanRepository {

    private MealplanCrudRepository mealplanCrudRepository;

    public Mealplan saveMealplan(final Mealplan mealplan) {
        return mealplanCrudRepository.save(mealplan);
    }

    @Transactional
    public void deleteMealplan(final Integer userId, final Long mealplanId) {
        mealplanCrudRepository.deleteByUserIdAndId(userId, mealplanId);
    }

    public List<Mealplan> getAllMealplans(final Integer userId) {
        return mealplanCrudRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteAllForUser(final Integer userId) {
        mealplanCrudRepository.deleteByUserId(userId);
    }
}
