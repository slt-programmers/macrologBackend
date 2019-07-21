package slt.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.database.FoodRepository;
import slt.database.MealRepository;
import slt.database.PortionRepository;
import slt.database.entities.Meal;
import slt.dto.AddMealRequest;
import slt.dto.MealDto;
import slt.dto.MyModelMapper;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/meals")
@Api(value = "meals")
public class MealService {

    @Autowired
    private MealRepository mealRepository;
    @Autowired
    private FoodRepository foodRepository;
    @Autowired
    private PortionRepository portionRepository;

    @Autowired
    private MyModelMapper myModelMapper;

    @ApiOperation(value = "Retrieve all meals")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MealDto>> getAllMeals() {

        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<Meal> allMeals = mealRepository.getAllMeals(userInfo.getUserId());

        List<MealDto> allMealsDto = allMeals.stream()
                .map(meal -> myModelMapper.getConfiguredMapper().map(meal, MealDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(allMealsDto);
    }

    @ApiOperation(value = "Save meal")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MealDto> storeMeal(@RequestBody AddMealRequest mealDto) {

        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        Meal map = myModelMapper.getConfiguredMapper().map(mealDto, Meal.class);

        if (mealRepository.findByName(userInfo.getUserId(), mealDto.getName()) != null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Meal meal = mealRepository.saveMeal(userInfo.getUserId(), map);

        return ResponseEntity.status(HttpStatus.CREATED).body(myModelMapper.getConfiguredMapper().map(meal,MealDto.class));
    }

    @ApiOperation(value = "Delete meal")
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteMeal(@PathVariable("id") Long mealId) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        mealRepository.deleteMeal(userInfo.getUserId(), mealId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
