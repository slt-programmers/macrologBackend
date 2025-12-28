package slt.database;

import lombok.AllArgsConstructor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.Food;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

interface FoodCrudRepository extends CrudRepository<Food, Long> {

    List<Food> findByUserIdOrderByNameAsc(final Long userId);

    Optional<Food> findByUserIdAndName(final Long userId, final String name);

    Optional<Food> findByUserIdAndId(final Long userId, final Long id);

    void deleteByUserId(final Long userId);
}

@Repository
@AllArgsConstructor
public class FoodRepository {

    private FoodCrudRepository foodCrudRepository;

    public List<Food> getAllFood(final Long userId) {
        return foodCrudRepository.findByUserIdOrderByNameAsc(userId);
    }

    public Food saveFood(final Food food) {
        return foodCrudRepository.save(food);
    }
    
    public Optional<Food> getFood(final Long userId, final String name) {
        return foodCrudRepository.findByUserIdAndName(userId, name);
    }

    public Optional<Food> getFoodById(final Long userId, final Long id) {
        return foodCrudRepository.findByUserIdAndId(userId, id);
    }

    @Transactional
    public void deleteAllForUser(final Long userId) {
        foodCrudRepository.deleteByUserId(userId);
    }
}

