package csl.rest;

import csl.database.FoodRepository;
import csl.database.PortionRepository;
import csl.database.model.Food;
import csl.dto.AddFoodRequest;
import csl.dto.FoodDto;
import csl.dto.Macro;
import csl.dto.PortionDto;
import csl.enums.MeasurementUnit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/food")
@Api(value = "food", description = "Operations pertaining to food in the macro logger applications")
public class FoodService {

    private final static FoodRepository foodRepository = new FoodRepository();
    private final static PortionRepository portionRepository = new PortionRepository();

    @ApiOperation(value = "Retrieve all stored foods")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllFood() {

        List<Food> allFood = foodRepository.getAllFood();
        List<FoodDto> allFoodDtos = new ArrayList<>();
        for (Food food : allFood) {
            allFoodDtos.add(createFoodDto(food,true));
        }

        allFoodDtos.sort(Comparator.comparing(FoodDto::getName));

        return ResponseEntity.ok(allFoodDtos);
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
            FoodDto foodDto = createFoodDto(food,true);

            return ResponseEntity.ok(foodDto);
        }
    }

    public FoodDto createFoodDto(Food food, boolean withPortions) {
        FoodDto foodDto = mapFoodToFoodDto(food);

        if (withPortions) {
            List<csl.database.model.Portion> foodPortions = portionRepository.getPortions(food.getId());
            for (csl.database.model.Portion portion : foodPortions) {
                PortionDto currDto = new PortionDto();
                currDto.setDescription(portion.getDescription());
                currDto.setGrams(portion.getGrams());
                currDto.setUnitMultiplier(portion.getUnitMultiplier());
                currDto.setId(portion.getId());

                Macro calculatedMacros = calculateMacro(food, portion);
                currDto.setMacros(calculatedMacros);
                foodDto.addPortion(currDto);
            }
        }
        return foodDto;
    }

    public static FoodDto mapFoodToFoodDto(Food food) {
        FoodDto foodDto = new FoodDto();
        foodDto.setName(food.getName());
        foodDto.setId(food.getId());
        foodDto.setMeasurementUnit(food.getMeasurementUnit());
        foodDto.setUnitGrams(food.getUnitGrams());
        foodDto.setUnitName(food.getUnitName());
        foodDto.setProtein(food.getProtein());
        foodDto.setCarbs(food.getCarbs());
        foodDto.setFat(food.getFat());
        return foodDto;
    }

    // Naar een util brengen:
    public static Macro calculateMacro(Food food, csl.database.model.Portion portion) {
        Macro calculatedMacros = new Macro();
        if (food.getMeasurementUnit().equals(MeasurementUnit.GRAMS)) {
            // FoodDto has been entered for 100g
                calculatedMacros.setCarbs(food.getCarbs() / 100 * portion.getGrams());
            calculatedMacros.setProtein(food.getProtein() / 100 * portion.getGrams());
            calculatedMacros.setFat(food.getFat() / 100 * portion.getGrams());
        } else {
            // FoodDto has been entered per unit
            calculatedMacros.setCarbs(food.getCarbs()  * portion.getUnitMultiplier());
            calculatedMacros.setProtein(food.getProtein()  * portion.getUnitMultiplier());
            calculatedMacros.setFat(food.getFat() * portion.getUnitMultiplier());
        }
        return calculatedMacros;
    }

    @ApiOperation(value = "Store new food with supplied macro per 100 grams")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity addFood(@RequestBody AddFoodRequest addFoodRequest) throws URISyntaxException {
        if (addFoodRequest.getId()!= null){
            // Update request
            Food newFood = new Food();
            newFood.setId(addFoodRequest.getId());
            newFood.setName(addFoodRequest.getName());
            newFood.setMeasurementUnit(addFoodRequest.getMeasurementUnit());
            if (newFood.getMeasurementUnit().equals(MeasurementUnit.UNIT)) {
                newFood.setUnitGrams(addFoodRequest.getUnitGrams());
                newFood.setUnitName(addFoodRequest.getUnitName());
            } else {
                newFood.setUnitGrams(100.0);
                newFood.setUnitName("gram");
            }

            newFood.setCarbs(addFoodRequest.getCarbs());
            newFood.setFat(addFoodRequest.getFat());
            newFood.setProtein(addFoodRequest.getProtein());


            foodRepository.updateFood(newFood);

            // remove portions not supported yet.
            for (PortionDto portionDto : addFoodRequest.getPortions()) {
                Long id = portionDto.getId();
                if (id != null){
                     // update portion
                    csl.database.model.Portion newPortion = new csl.database.model.Portion();
                    newPortion.setId(portionDto.getId());
                    newPortion.setDescription(portionDto.getDescription());
                    newPortion.setGrams(portionDto.getGrams());
                    newPortion.setUnitMultiplier(portionDto.getUnitMultiplier());
                    portionRepository.updatePortion(newFood.getId(),newPortion);

                } else {
                    // add portion
                    csl.database.model.Portion newPortion = new csl.database.model.Portion();
                    newPortion.setDescription(portionDto.getDescription());
                    newPortion.setGrams(portionDto.getGrams());
                    newPortion.setUnitMultiplier(portionDto.getUnitMultiplier());
                    portionRepository.addPortion(newFood.getId(),newPortion);
                }
            }
            return ResponseEntity.status(HttpStatus.CREATED).build();


        } else {
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
                    newFood.setUnitName("gram");
                }

                newFood.setCarbs(addFoodRequest.getCarbs());
                newFood.setFat(addFoodRequest.getFat());
                newFood.setProtein(addFoodRequest.getProtein());

                int insertedRows = foodRepository.insertFood(newFood);
                if (insertedRows == 1 && addFoodRequest.getPortions() != null && !addFoodRequest.getPortions().isEmpty()) {
                    Food addedFood = foodRepository.getFood(addFoodRequest.getName());
                    for (PortionDto portionDto : addFoodRequest.getPortions()) {
                        csl.database.model.Portion newPortion = new csl.database.model.Portion();
                        newPortion.setDescription(portionDto.getDescription());
                        newPortion.setGrams(portionDto.getGrams());
                        newPortion.setUnitMultiplier(portionDto.getUnitMultiplier());

                        portionRepository.addPortion(addedFood.getId(), newPortion);
                    }
                }

                return ResponseEntity.status(HttpStatus.CREATED).build();
            }
        }
    }

//    @ApiOperation(value = "Adds an portion for a food")
//    @RequestMapping(value = "/{id}/alias",
//            method = POST,
//            headers = {"Content-Type=application/json"})
//    public ResponseEntity addPortion(@PathVariable("id") Long foodId,
//                                     @RequestBody AddPortionRequest addUnitAliasRequest) throws URISyntaxException {
//        FoodDto food = foodRepository.getFoodById(foodId);
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
