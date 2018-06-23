package csl.rest;

import csl.database.FoodRepository;
import csl.database.model.Food;
import csl.dto.Macro;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
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

    // Post with params
//    @CrossOrigin(origins = {"*", "http://localhost:4200"})
//    @RequestMapping(value = "/newFood",
//            method = POST,
//            headers = {"Content-Type=application/json"},
//            params = {"name"})
//    public void insertFood(@RequestParam("name") String name) {
//
//        Food food = new Food(name, null, null, null, null, null, null);
//
//        int insertedRows = foodRepository.insertFood(food);
//        System.out.println(insertedRows + " row(s) inserted");
//    }
//
//    // Example without params with String JSON
//    @CrossOrigin(origins = {"*", "http://localhost:4200"})
//    @RequestMapping(value = "/newFoodTwo",
//            method = POST,
//            headers = {"Content-Type=application/json"})
//    public void insertFoodTwo(@RequestBody Food food) {
//        System.out.println("Inside new food 2");
//        System.out.println(food);
//        int insertedRows = foodRepository.insertFood(food);
////        System.out.println(insertedRows + " row(s) inserted");
//    }


    @ApiOperation(value = "Store new food with supplied macro per 100 grams")
    @RequestMapping(value = "/{name}",
            method = POST,
            headers = {"Content-Type=application/json"})
    public void storeFood(@PathVariable("name") String name,@RequestBody Macro macrovalues) {
        System.out.println("Inside new food 2");
        Food newFood = new Food();
        newFood.setName(name);
        newFood.setCarbs(macrovalues.getCarbs());
        newFood.setFat(macrovalues.getFat());
        newFood.setProtein(macrovalues.getProteins());
        newFood.setOptionalGrams(100);
        newFood.setUnit("default");
        newFood.setUnitName("per 100 grams");
        int insertedRows = foodRepository.insertFood(newFood);
    }
}
