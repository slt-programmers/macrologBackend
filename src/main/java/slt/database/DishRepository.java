package slt.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.Dish;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

interface DishCrudRepository extends CrudRepository<Dish, Long> {

    void deleteByUserIdAndId(final Long userId, final Long dishId);

    void deleteByUserId(final Long userId);

    List<Dish> findByUserId(final Long userId);

    Optional<Dish> findByUserIdAndName(final Long userId, final String name);
}

@Repository
public class DishRepository {

    @Autowired
    DishCrudRepository dishCrudRepository;

    public Dish saveDish(final Long userId, final Dish dish) {
        dish.setUserId(userId);
        return dishCrudRepository.save(dish);
    }

    public Optional<Dish> findByName(final Long userId, final String name) {
        return dishCrudRepository.findByUserIdAndName(userId, name);
    }

    @Transactional
    public void deleteDish(final Long userId, final Long dishId) {
        dishCrudRepository.deleteByUserIdAndId(userId, dishId);
    }

    public List<Dish> getAllDishes(final Long userId) {
        return dishCrudRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteAllForUser(final Long userId) {
        dishCrudRepository.deleteByUserId(userId);
    }

}