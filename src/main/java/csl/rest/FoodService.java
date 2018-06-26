package csl.rest;

import csl.database.FoodRepository;
import csl.database.model.Food;
import csl.dto.AddFoodMacroRequest;
import csl.dto.AddUnitAliasRequest;
import csl.dto.FoodMacros;
import csl.dto.Macro;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/food")
@Api(value = "food", description = "Operations pertaining to food in the macro logger applications")
public class FoodService {

    private FoodRepository foodRepository = new FoodRepository();

    @ApiOperation(value = "Retrieve all stored foods")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllFood() {
        return ResponseEntity.ok(foodRepository.getAllFood());
    }

    @ApiOperation(value = "Retrieve information about specific food")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/{name}",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getFoodInformation(@PathVariable("name") String name) {

        List<Food> foodList = foodRepository.getFood(name);
        if (foodList.size() != 1) {
            return ResponseEntity.noContent().build();
        } else {
            List<FoodMacros> foodMacros = new ArrayList<>();
            for (Food food : foodList) {
                FoodMacros curr = new FoodMacros();
                curr.setName(food.getName());
                curr.setAmountUnit(food.getAmountUnit());

                Macro macro = new Macro();
                macro.setCarbs(food.getCarbs());
                macro.setFat(food.getFat());
                macro.setProteins(food.getProtein());
                curr.addMacroPerUnit(food.getAmountNumber(), macro);


                foodMacros.add(curr);
            }
            return ResponseEntity.ok(foodMacros.get(0));
        }
    }

    @ApiOperation(value = "Store new food with supplied macro per 100 grams")
    @RequestMapping(value = "/{name}",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity storeFood(@PathVariable("name") String name,
                                    @RequestBody AddFoodMacroRequest addFoodMacroRequest) throws URISyntaxException {
        List<Food> foodList = foodRepository.getFood(name);
        if (foodList.size() > 0) {
            return ResponseEntity.badRequest().build();
        } else {
            Food newFood = new Food();
            newFood.setName(name);
            newFood.setAmountUnit(addFoodMacroRequest.getDefaultUnitname());
            newFood.setAmountNumber(addFoodMacroRequest.getDefaultAmount());

            Macro macroPerUnit = addFoodMacroRequest.getMacroPerUnit();
            newFood.setCarbs(macroPerUnit.getCarbs());
            newFood.setFat(macroPerUnit.getFat());
            newFood.setProtein(macroPerUnit.getProteins());

            int insertedRows = foodRepository.insertFood(newFood);

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .buildAndExpand(newFood.getName()).toUri();

            return ResponseEntity.created(location).build();
        }
    }

    @ApiOperation(value = "Adds an alias for a food")
    @RequestMapping(value = "/{name}/alias",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity addAlias(@PathVariable("name") String name,
                                   @RequestBody AddUnitAliasRequest addUnitAliasRequest) throws URISyntaxException {
        List<Food> foodList = foodRepository.getFood(name);
        if (foodList.size() > 0) {
            return ResponseEntity.badRequest().build();
        } else {


//            int insertedRows = foodRepository.insertFood(newFood);

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .buildAndExpand("da").toUri();

            return ResponseEntity.created(location).build();
        }
    }

}
