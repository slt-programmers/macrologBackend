package csl.rest;

import csl.database.MealRepository;
import csl.database.model.Meal;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/meals")
@Api(value = "meals", description = "Operations pertaining to meals in the macro logger applications")
public class MealService {

    private MealRepository mealRepository = new MealRepository();
    private static final Logger LOGGER = LoggerFactory.getLogger(MealService.class);


    @ApiOperation(value = "Retrieve all meals")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllMeals() {
        List<Meal> allMeals = mealRepository.getAllMeals();
        return ResponseEntity.ok(allMeals);
    }

    @ApiOperation(value = "Insert meal")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity storeMeal(@RequestBody Meal meal) {
        if (meal.getId() == null) {
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
}
