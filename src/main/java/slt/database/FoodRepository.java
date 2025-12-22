package slt.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import slt.database.entities.Food;

import jakarta.transaction.Transactional;
import java.util.List;

interface FoodCrudRepository extends CrudRepository<Food, Long> {

    List<Food> findByUserId(final Long userId);

    List<Food> findByUserIdAndName(final Long userId, String name);

    List<Food> findByUserIdAndId(final Long userId, Long id);

    void deleteByUserId(final Long userId);
}

@Repository
public class FoodRepository {

    @Autowired
    private FoodCrudRepository foodCrudRepository;

    public List<Food> getAllFood(final Long userId) {
        return foodCrudRepository.findByUserId(userId);
    }

    public Food saveFood(final Long userId, Food food) {
        food.setUserId(userId);
        return foodCrudRepository.save(food);
    }
    
    public Food getFood(final Long userId, String name) {
        List<Food> queryResults = foodCrudRepository.findByUserIdAndName(userId, name);
        return queryResults.isEmpty() ? null : queryResults.getFirst();
    }

    public Food getFoodById(final Long userId, Long id) {
        List<Food> queryResults = foodCrudRepository.findByUserIdAndId(userId, id);
        Assert.isTrue(queryResults.size() <= 1, "More than one food was found");
        return queryResults.isEmpty() ? null : queryResults.getFirst();
    }

    @Transactional
    public void deleteAllForUser(final Long userId) {
        foodCrudRepository.deleteByUserId(userId);
    }
}

