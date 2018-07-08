package csl.rest;

import csl.database.FoodRepository;
import csl.database.PortionRepository;
import csl.database.model.Food;
import csl.dto.AddFoodRequest;
import csl.dto.Portion;
import csl.enums.MeasurementUnit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/food")
@Api(value = "food", description = "Operations pertaining to food in the macro logger applications")
public class FoodService {

    private FoodRepository foodRepository = new FoodRepository();
    private PortionRepository portionRepository = new PortionRepository();

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
    @RequestMapping(value = "/{id}",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getFoodInformation(@PathVariable("id") Long id) {

        Food food = foodRepository.getFoodById(id);
        if (food == null) {
            return ResponseEntity.noContent().build();
        } else {
//            FoodMacros curr = new FoodMacros();
//            curr.setFoodId(food.getId());
//            curr.setName(food.getName());
//            curr.setAmountUnit(food.getMeasurementUnit().toString());
//
//            Macro macro = new Macro();
//            macro.setCarbs(food.getCarbs());
//            macro.setFat(food.getFat());
//            macro.setProteins(food.getProtein());
//            curr.addMacroPerUnit(food.getUnitGrams(), macro);

            csl.dto.Food foodDto = new csl.dto.Food();
            foodDto.setName(food.getName());
            foodDto.setMeasurementUnit(food.getMeasurementUnit());
            foodDto.setUnitGrams(food.getUnitGrams());
            foodDto.setUnitName(food.getUnitName());
            foodDto.setProtein(food.getProtein());
            foodDto.setCarbs(food.getCarbs());
            foodDto.setFat(food.getFat());

            List<csl.database.model.Portion> foodPortions = portionRepository.getPortions(food.getId());
            for (csl.database.model.Portion portion : foodPortions) {
                csl.dto.Portion currDto = new csl.dto.Portion();
                currDto.setDescription(portion.getDescription());
                currDto.setGrams(portion.getGrams());
                currDto.setUnitMultiplier(portion.getUnitMultiplier());
                currDto.setId(portion.getId());
//
//                currDto.setAliasCarbs(food.getCarbs() / 100 * currDto.getAmountNumber());
//                currDto.setAliasProtein(food.getProtein() / 100 * currDto.getAmountNumber());
//                currDto.setAliasFat(food.getFat() / 100 * currDto.getAmountNumber());

                foodDto.addPortion(currDto);
            }

            return ResponseEntity.ok(foodDto);
        }
    }

    @ApiOperation(value = "Store new food with supplied macro per 100 grams")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity addFood(@RequestBody AddFoodRequest addFoodRequest) throws URISyntaxException {
        Food food = foodRepository.getFood(addFoodRequest.getName());
        if (food != null) {
            String errorMessage = "This food is already in your database";
            return ResponseEntity.badRequest().body(errorMessage);
        } else {
            Food newFood = new Food();
            newFood.setName(addFoodRequest.getName());
            newFood.setMeasurementUnit(addFoodRequest.getMeasurementUnit());
            if (newFood.getMeasurementUnit().equals(MeasurementUnit.UNIT)) {
                newFood.setUnitGrams(addFoodRequest.getUnitGrams());
                newFood.setUnitName(addFoodRequest.getUnitName());
            } else {
                newFood.setUnitGrams(100.0);
                newFood.setUnitName("grams");
            }

            newFood.setCarbs(addFoodRequest.getCarbs());
            newFood.setFat(addFoodRequest.getFat());
            newFood.setProtein(addFoodRequest.getProtein());

            int insertedRows = foodRepository.insertFood(newFood);
            if (insertedRows == 1 && addFoodRequest.getPortions() != null && !addFoodRequest.getPortions().isEmpty()) {
                Food addedFood = foodRepository.getFood(addFoodRequest.getName());
                for (Portion portionDto : addFoodRequest.getPortions()) {
                    csl.database.model.Portion newPortion = new csl.database.model.Portion();
                    newPortion.setDescription(portionDto.getDescription());
                    newPortion.setGrams(portionDto.getGrams());
                    newPortion.setUnitMultiplier(portionDto.getUnitMultiplier());

                    portionRepository.addPortion(addedFood, newPortion);
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
    }

//    @ApiOperation(value = "Adds an portion for a food")
//    @RequestMapping(value = "/{id}/alias",
//            method = POST,
//            headers = {"Content-Type=application/json"})
//    public ResponseEntity addPortion(@PathVariable("id") Long foodId,
//                                     @RequestBody AddPortionRequest addUnitAliasRequest) throws URISyntaxException {
//        Food food = foodRepository.getFoodById(foodId);
//        if (food == null) {
//            return ResponseEntity.badRequest().build();
//        } else {
//
//            FoodAlias foodAlias = new FoodAlias();
//            foodAlias.setAliasname(addUnitAliasRequest.getDescription());
//            foodAlias.setAmountNumber(addUnitAliasRequest.getGrams());
//            foodAlias.setAmountUnit(addUnitAliasRequest.getUnitMultiplier());
//            foodAlias.setFoodId(foodId);
//            portionRepository.addFoodAlias(food, foodAlias);
//
////            int insertedRows = foodRepository.insertFood(newFood);
//
//
//            return ResponseEntity.status(HttpStatus.CREATED).build();
//        }
//    }
}
