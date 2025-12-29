package slt.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import slt.database.FoodRepository;
import slt.dto.FoodDto;
import slt.exceptions.NotFoundException;
import slt.exceptions.ValidationException;
import slt.mapper.FoodMapper;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class FoodService {

    private FoodRepository foodRepository;

    private final FoodMapper foodMapper = FoodMapper.INSTANCE;

    public List<FoodDto> getAllFood(final Long userId) {
        final var allFood = foodRepository.getAllFood(userId);
        return foodMapper.map(allFood);
    }

    public FoodDto getFoodById(final Long userId, final Long foodId) {
        final var optionalFood = foodRepository.getFoodById(userId, foodId);
        return foodMapper.map(optionalFood.orElseThrow(() ->
                new NotFoundException("Food with id [" + foodId + "] not found for user [" + userId + "].")));
    }

    public FoodDto saveFood(final Long userId, final FoodDto foodDto) {
        if (foodDto.getId() == null) {
            final var existingFoodWithSameName = foodRepository.getFood(userId, foodDto.getName());
            if (existingFoodWithSameName.isPresent()) {
                log.error("This food is already in your database");
                throw new ValidationException("Food with name [" + foodDto.getName() + "] already exists for user [" + userId + "].");
            }
        }

        final var food = foodMapper.map(foodDto, userId);
        final var savedFood = foodRepository.saveFood(food);
        return foodMapper.map(savedFood);
    }
}
