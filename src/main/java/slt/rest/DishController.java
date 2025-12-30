package slt.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.dto.DishDto;
import slt.security.ThreadLocalHolder;
import slt.service.DishService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dishes")
@AllArgsConstructor
public class DishController {

    private DishService dishService;

    @GetMapping
    public ResponseEntity<List<DishDto>> getAllDishes() {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var allDishDtos= dishService.getAllDishes(userInfo.getUserId());
        return ResponseEntity.ok(allDishDtos);
    }

    @PostMapping
    public ResponseEntity<DishDto> postDish(@RequestBody final DishDto dishDto) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var savedDish = dishService.saveDish(userInfo.getUserId(), dishDto);
        return ResponseEntity.ok(savedDish);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteDish(@PathVariable("id") final Long dishId) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        dishService.deleteDish(userInfo.getUserId(), dishId);
        return ResponseEntity.ok().build();
    }
}
