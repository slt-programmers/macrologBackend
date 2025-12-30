package slt.rest;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.dto.MealplanDto;
import slt.security.ThreadLocalHolder;
import slt.service.MealplanService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/mealplans")
public class MealplanController {

    private MealplanService mealplanService;

    @GetMapping
    public ResponseEntity<List<MealplanDto>> getAllMealplans() {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var allMealplanDtos = mealplanService.getAllMealplans(userInfo.getUserId());
        return ResponseEntity.ok(allMealplanDtos);
    }

    @PostMapping
    public ResponseEntity<MealplanDto> postMealplan(@RequestBody final MealplanDto mealplanDto) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var savedMealplan = mealplanService.saveMealplan(userInfo.getUserId(), mealplanDto);
        return ResponseEntity.ok(savedMealplan);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteMealplan(@PathVariable("id") final Long mealplanId) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        mealplanService.deleteMealplan(userInfo.getUserId(), mealplanId);
        return ResponseEntity.ok().build();
    }

}
