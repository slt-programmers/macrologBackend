package slt.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.database.FoodRepository;
import slt.dto.*;
import slt.mapper.FoodMapper;
import slt.security.ThreadLocalHolder;
import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/food")
public class FoodService {

    private FoodRepository foodRepository;

    private final FoodMapper foodMapper = FoodMapper.INSTANCE;

    @GetMapping
    public ResponseEntity<List<FoodDto>> getAllFood() {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var allFood = foodRepository.getAllFood(userInfo.getUserId());
        return ResponseEntity.ok(foodMapper.map(allFood));
    }

    @GetMapping(path = "{id}")
    public ResponseEntity<FoodDto> getFoodById(@PathVariable("id") Long id) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var food = foodRepository.getFoodById(userInfo.getUserId(), id);
        if (food == null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(foodMapper.map(food));
        }
    }

    @PostMapping
    public ResponseEntity<FoodDto> postFood(@RequestBody FoodDto foodDto) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();

        if (foodDto.getId() == null) {
            final var existingFoodWithSameName = foodRepository.getFood(userInfo.getUserId(), foodDto.getName());
            if (existingFoodWithSameName != null) {
                log.error("This food is already in your database");
                return ResponseEntity.badRequest().build();
            }
        }

        final var food = foodMapper.map(foodDto);
        final var savedFood = foodRepository.saveFood(userInfo.getUserId(), food);
        return ResponseEntity.ok(foodMapper.map(savedFood));
    }

}
