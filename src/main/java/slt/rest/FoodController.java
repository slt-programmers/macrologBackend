package slt.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.dto.*;
import slt.security.ThreadLocalHolder;
import slt.service.FoodService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/food")
public class FoodController {

    private FoodService foodService;

    @GetMapping
    public ResponseEntity<List<FoodDto>> getAllFood() {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var allFood = foodService.getAllFood(userInfo.getUserId());
        return ResponseEntity.ok(allFood);
    }

    @GetMapping(path = "{id}")
    public ResponseEntity<FoodDto> getFoodById(@PathVariable("id") Long id) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var food = foodService.getFoodById(userInfo.getUserId(), id);
        return ResponseEntity.ok(food);
    }

    @PostMapping
    public ResponseEntity<FoodDto> postFood(@RequestBody final FoodDto foodDto) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var savedFood = foodService.saveFood(userInfo.getUserId(), foodDto);
        return ResponseEntity.ok(savedFood);
    }

}
