package slt.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import slt.database.entities.Food;

import javax.transaction.Transactional;
import java.util.List;

interface FoodCrudRepository extends CrudRepository<Food, Integer> {

    List<Food> findByUserId(Integer userId);

    List<Food> findByUserIdAndName(Integer userId, String name);

    List<Food> findByUserIdAndId(Integer userId, Long id);

    void deleteByUserId(Integer userId);
}

@Repository
public class FoodRepository {

    @Autowired
    private FoodCrudRepository foodCrudRepository;

    public List<Food> getAllFood(Integer userId) {
        return foodCrudRepository.findByUserId(userId);
    }

    public Food saveFood(Integer userId, Food food) {
        food.setUserId(userId);
        return foodCrudRepository.save(food);
    }
    
    public Food getFood(Integer userId, String name) {
        List<Food> queryResults = foodCrudRepository.findByUserIdAndName(userId, name);
        return queryResults.isEmpty() ? null : queryResults.get(0);
    }

    public Food getFoodById(Integer userId, Long id) {
        List<Food> queryResults = foodCrudRepository.findByUserIdAndId(userId, id);
        Assert.isTrue(queryResults.size() <= 1, "More than one food was found");
        return queryResults.isEmpty() ? null : queryResults.get(0);
    }

    @Transactional
    public void deleteAllForUser(Integer userId) {
        foodCrudRepository.deleteByUserId(userId);
    }
}

