package slt.rest;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.database.MealplanRepository;
import slt.database.entities.Mealplan;
import slt.mapper.MyModelMapper;
import slt.security.ThreadLocalHolder;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/mealplans")
public class MealplanController {

    @Autowired
    private MealplanRepository mealplanRepository;

    @Autowired
    private MyModelMapper myModelMapper;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Mealplan>> getAllMealplans() {
        // TODO write separate DTO instead of using entity
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var allMealplans = mealplanRepository.getAllMealplans(userInfo.getUserId());
//        List<MealplanDto> allMealplansDto = allMealplans.stream()
//                .map(mealplan -> myModelMapper.getConfiguredMapper().map(mealplan, MealplanDto.class))
//                .toList();

        return ResponseEntity.ok(allMealplans);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mealplan> postMealplan(@RequestBody Mealplan mealplan) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        // TODO controles oid?
        final var savedPlan = mealplanRepository.saveMealplan(userInfo.getUserId(), mealplan);
        return ResponseEntity.ok(savedPlan);
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mealplan> putMealplan(@RequestBody Mealplan mealplan) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        // TODO controles oid?
        final var savedPlan = mealplanRepository.saveMealplan(userInfo.getUserId(), mealplan);
        return ResponseEntity.ok(savedPlan);
    }
}
