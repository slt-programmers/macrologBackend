package slt.rest;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import slt.database.MealplanRepository;
import slt.database.entities.Mealplan;
import slt.dto.MealplanDto;
import slt.dto.requests.MealplanRequest;
import slt.mapper.MealplanMapper;
import slt.security.ThreadLocalHolder;

import java.util.List;

@RestController
@Component
@AllArgsConstructor
@RequestMapping("/mealplans")
public class MealplanController {

    private final MealplanRepository mealplanRepository;

    private final MealplanMapper mealplanMapper = MealplanMapper.INSTANCE;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MealplanDto>> getAllMealplans() {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var allMealplans = mealplanRepository.getAllMealplans(userInfo.getUserId());
        final var allMealplanDtos = allMealplans.stream().map(mealplanMapper::map).toList();
        return ResponseEntity.ok(allMealplanDtos);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mealplan> postMealplan(@RequestBody MealplanRequest request) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        // TODO controles
        final var mealplan = mealplanMapper.map(request, userInfo.getUserId());
        final var savedPlan = mealplanRepository.saveMealplan(mealplan);
        return ResponseEntity.ok(savedPlan);
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MealplanDto> putMealplan(@RequestBody MealplanRequest request) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        // TODO controles
        final var mealplan = mealplanMapper.map(request, userInfo.getUserId());
        final var savedPlan = mealplanRepository.saveMealplan(mealplan);
        return ResponseEntity.ok(mealplanMapper.map(savedPlan));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteMealplan(@PathVariable("id") Long mealplanId) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        // TODO controles oid?
        mealplanRepository.deleteMealplan(userInfo.getUserId(), mealplanId);
        return ResponseEntity.ok().build();
    }
}
