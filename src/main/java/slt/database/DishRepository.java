package slt.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.Dish;

import jakarta.transaction.Transactional;
import java.util.List;

interface DishCrudRepository extends CrudRepository<Dish, Long> {

    void deleteByUserIdAndId(Integer userId, Long dishId);

    void deleteByUserId(Integer userId);

    List<Dish> findByUserId(Integer userId);

    Dish findByUserIdAndName(Integer userId, String name);
}

@Repository
public class DishRepository {

    @Autowired
    DishCrudRepository dishCrudRepository;

    public Dish saveDish(Integer userId, Dish dish) {
        dish.setUserId(userId);
        return dishCrudRepository.save(dish);
    }

    public Dish findByName(Integer userId, String name) {
        return dishCrudRepository.findByUserIdAndName(userId,name);
    }

    @Transactional
    public void deleteDish(Integer userId, Long dishId) {
        dishCrudRepository.deleteByUserIdAndId(userId, dishId);
    }

    public List<Dish> getAllDishes(Integer userId) {
        return dishCrudRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteAllForUser(Integer userId) {
        dishCrudRepository.deleteByUserId(userId);
    }

}