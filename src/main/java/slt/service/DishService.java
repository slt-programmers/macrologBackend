package slt.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import slt.database.DishRepository;
import slt.dto.DishDto;
import slt.exceptions.ValidationException;
import slt.mapper.DishMapper;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class DishService {

    private DishRepository dishRepository;

    private final DishMapper dishMapper = DishMapper.INSTANCE;

    public List<DishDto> getAllDishes(final Long userId) {
        final var allDishes = dishRepository.getAllDishes(userId);
        return dishMapper.map(allDishes);
    }

    public DishDto saveDish(final Long userId, final DishDto dishDto) {
        final var optionalDishWithSameName = dishRepository.findByName(userId, dishDto.getName());
        validateUniqueDishName(optionalDishWithSameName.isPresent(), dishDto.getId() == null);
        final var dish = dishMapper.map(dishDto, userId);
        final var savedDish = dishRepository.saveDish(dish);
        return dishMapper.map(savedDish);
    }

    public void deleteDish(final Long userId, final Long dishId) {
        dishRepository.deleteDish(userId, dishId);
    }

    private void validateUniqueDishName(final Boolean isPresentWithSameName, final Boolean isNewDishRequest) {
        if (isPresentWithSameName && isNewDishRequest) {
            log.debug("Dish with name already exists");
            throw new ValidationException("Dish with name already exists.");
        }
    }
}
