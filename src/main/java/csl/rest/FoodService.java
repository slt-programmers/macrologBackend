package csl.rest;

import csl.database.FoodRepository;
import csl.database.model.Food;
import csl.dto.FoodMacros;
import csl.dto.Macro;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by Carmen on 18-3-2018.
 */
@RestController
@RequestMapping("/food")
@Api(value="food", description="Operations pertaining to food in the macro logger applications")
public class FoodService {

    private FoodRepository foodRepository = new FoodRepository();

    @ApiOperation(value = "Retrieve all stored foods")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Food> getAllFood() {
        return foodRepository.getAllFood();
    }

    @ApiOperation(value = "Retrieve information about specific food")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/{name}",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FoodMacros> getFoodInformation(@PathVariable("name") String name) {

        List<Food> foodList =  foodRepository.getFood(name);
        List<FoodMacros>foodMacros = new ArrayList<>();
        for (Food food : foodList) {
            FoodMacros curr = new FoodMacros();
            curr.setName(food.getName());
            Macro macro = new Macro();
            macro.setCarbs(food.getCarbs());
            macro.setFat(food.getFat());
            macro.setProteins(food.getProtein());
            curr.addMacroPerUnit("100g",macro);
            foodMacros.add(curr);
        }

        return foodMacros;
    }

    @ApiOperation(value = "Store new food with supplied macro per 100 grams")
    @RequestMapping(value = "/{name}",
            method = POST,
            headers = {"Content-Type=application/json"})
    public void storeFood(@PathVariable("name") String name,@RequestBody Macro macrovalues) {
        Food newFood = new Food();
        newFood.setName(name);
        newFood.setCarbs(macrovalues.getCarbs());
        newFood.setFat(macrovalues.getFat());
        newFood.setProtein(macrovalues.getProteins());
        newFood.setUnit("default");
        int insertedRows = foodRepository.insertFood(newFood);
    }
}
