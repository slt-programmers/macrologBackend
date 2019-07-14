package slt.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.database.FoodRepository;
import slt.database.MealRepository;
import slt.database.PortionRepository;
import slt.database.model.Food;
import slt.database.model.Ingredient;
import slt.database.model.Meal;
import slt.dto.IngredientDto;
import slt.dto.MealDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;

import java.util.ArrayList;
import java.util.List;

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

    @ApiOperation(value = "Retrieve all meals")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllMeals() {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<Meal> allMeals = mealRepository.getAllMeals(userInfo.getUserId());
        List<MealDto> allMealDtos = mapToDto(userInfo.getUserId(), allMeals);
        return ResponseEntity.ok(allMealDtos);
    }

    @ApiOperation(value = "Insert meal")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity storeMeal(@RequestBody Meal meal) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        if (meal.getId() == null) {
            System.out.println(meal);
            mealRepository.insertMeal(userInfo.getUserId(), meal);
        } else {
            mealRepository.updateMeal(userInfo.getUserId(), meal);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @ApiOperation(value = "Delete meal")
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteMeal(@PathVariable("id") Long mealId) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        mealRepository.deleteMeal(userInfo.getUserId(), mealId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private List<MealDto> mapToDto(Integer userId, List<Meal> meals) {
        List<MealDto> mealDtos = new ArrayList<>();

        for (Meal meal : meals) {
            MealDto mealDto = new MealDto();
            mealDto.setId(meal.getId());
            mealDto.setName(meal.getName());
            List<IngredientDto> ingredientDtos = new ArrayList<>();
            for (Ingredient ingredient : meal.getIngredients()) {
                IngredientDto ingredientDto = new IngredientDto();
                ingredientDto.setId(ingredient.getId());
                Long foodId = ingredient.getFoodId();
                ingredientDto.setFoodId(foodId);
                Food food = foodRepository.getFoodById(userId, foodId);
                ingredientDto.setFood(FoodService.mapFoodToFoodDto(food));

                Long portionId = ingredient.getPortionId();
                if (portionId != null && portionId != 0) {
                    ingredientDto.setPortionId(portionId);
                    ingredientDto.setPortion(FoodService.mapPortionToPortionDto(portionRepository.getPortion(foodId, portionId), food));
                }
                ingredientDto.setMultiplier(ingredient.getMultiplier());

                ingredientDtos.add(ingredientDto);
            }
            mealDto.setIngredients(ingredientDtos);

            mealDtos.add(mealDto);
        }

        return mealDtos;
    }
}
