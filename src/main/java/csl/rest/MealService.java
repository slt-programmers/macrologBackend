package csl.rest;

import csl.database.FoodRepository;
import csl.database.MealRepository;
import csl.database.PortionRepository;
import csl.database.model.Food;
import csl.database.model.Ingredient;
import csl.database.model.Meal;
import csl.dto.IngredientDto;
import csl.dto.MealDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/meals")
@Api(value = "meals", description = "Operations pertaining to meals in the macro logger applications")
public class MealService {

    private MealRepository mealRepository = new MealRepository();
    private FoodRepository foodRepository = new FoodRepository();
    private PortionRepository portionRepository = new PortionRepository();
    private static final Logger LOGGER = LoggerFactory.getLogger(MealService.class);


    @ApiOperation(value = "Retrieve all meals")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllMeals() {
        List<Meal> allMeals = mealRepository.getAllMeals();
        List<MealDto> allMealDtos = mapToDto(allMeals);
        return ResponseEntity.ok(allMeals);
    }

    @ApiOperation(value = "Insert meal")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity storeMeal(@RequestBody Meal meal) {
        if (meal.getId() == null) {
            System.out.println(meal);
            mealRepository.insertMeal(meal);
        } else {
            mealRepository.updateMeal(meal);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @ApiOperation(value = "Delete meal")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/{id}",
            method = DELETE,
            headers = {"Content-Type=application/json"})
    public ResponseEntity deleteMeal(@PathVariable("id") Long mealId) {
        mealRepository.deleteMeal(mealId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private List<MealDto> mapToDto(List<Meal> meals) {
        List<MealDto> mealDtos = new ArrayList<>();

        for(Meal meal: meals) {
            MealDto mealDto = new MealDto();
            mealDto.setId(meal.getId());
            mealDto.setName(meal.getName());
            List<IngredientDto> ingredientDtos = new ArrayList<>();
            for(Ingredient ingredient: meal.getIngredients()) {
                IngredientDto ingredientDto = new IngredientDto();
                ingredientDto.setId(ingredient.getId());
                Long foodId = ingredient.getFoodId();
                ingredientDto.setFoodId(foodId);
                Food food = foodRepository.getFoodById(foodId);
                ingredientDto.setFood(FoodService.mapFoodToFoodDto(food));

                Long portionId = ingredient.getPortionId();
                if(portionId != null && portionId != 0) {
                    ingredientDto.setPortionId(portionId);
                    ingredientDto.setPortion(FoodService.mapPortionToPortionDto(portionRepository.getPortion(portionId), food));
                }
                ingredientDto.setMultiplier(ingredient.getMultiplier());

                ingredientDtos.add(ingredientDto);
            }
            mealDto.setIngredientDtos(ingredientDtos);

            mealDtos.add(mealDto);
        }

        return mealDtos;
    }
}
