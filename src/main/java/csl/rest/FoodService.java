package csl.rest;

import csl.database.FoodRepository;
import csl.database.PortionRepository;
import csl.database.model.Food;
import csl.database.model.Portion;
import csl.dto.AddFoodRequest;
import csl.dto.FoodDto;
import csl.dto.Macro;
import csl.dto.PortionDto;
import csl.security.ThreadLocalHolder;
import csl.security.UserInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/food")
@Api(value = "food")
public class FoodService {

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private PortionRepository portionRepository;

    @ApiOperation(value = "Retrieve all stored foods")
    @RequestMapping(value = "",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllFood() {

        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<Food> allFood = foodRepository.getAllFood(userInfo.getUserId());
        List<FoodDto> allFoodDtos = new ArrayList<>();
        for (Food food : allFood) {
            allFoodDtos.add(createFoodDto(food, true));
        }

        allFoodDtos.sort(Comparator.comparing(FoodDto::getName));
        return ResponseEntity.ok(allFoodDtos);
    }

    @ApiOperation(value = "Retrieve information about specific food")
    @RequestMapping(value = "/{id}",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getFoodInformation(@PathVariable("id") Long id) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        Food food = foodRepository.getFoodById(userInfo.getUserId(), id);
        if (food == null) {
            return ResponseEntity.noContent().build();
        } else {
            FoodDto foodDto = createFoodDto(food, true);
            return ResponseEntity.ok(foodDto);
        }
    }

    @ApiOperation(value = "Store new food with supplied macro per 100 grams")
    @RequestMapping(value = "",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity addFood(@RequestBody AddFoodRequest addFoodRequest) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        if (addFoodRequest.getId() != null) {
            // Update request
            Food newFood = new Food();
            newFood.setId(addFoodRequest.getId());
            newFood.setName(addFoodRequest.getName());
            newFood.setCarbs(addFoodRequest.getCarbs());
            newFood.setFat(addFoodRequest.getFat());
            newFood.setProtein(addFoodRequest.getProtein());

            foodRepository.updateFood(userInfo.getUserId(), newFood);

            // remove portions not supported yet.
            for (PortionDto portionDto : addFoodRequest.getPortions()) {
                Long id = portionDto.getId();
                if (id != null) {
                    // update portion
                    csl.database.model.Portion newPortion = new csl.database.model.Portion();
                    newPortion.setId(portionDto.getId());
                    newPortion.setDescription(portionDto.getDescription());
                    newPortion.setGrams(portionDto.getGrams());
                    portionRepository.updatePortion(newFood.getId(), newPortion);

                } else {
                    // add portion
                    csl.database.model.Portion newPortion = new csl.database.model.Portion();
                    newPortion.setDescription(portionDto.getDescription());
                    newPortion.setGrams(portionDto.getGrams());
                    portionRepository.addPortion(newFood.getId(), newPortion);
                }
            }
            return ResponseEntity.status(HttpStatus.CREATED).build();


        } else {
            Food food = foodRepository.getFood(userInfo.getUserId(), addFoodRequest.getName());
            if (food != null) {
                String errorMessage = "This food is already in your database";
                return ResponseEntity.badRequest().body(errorMessage);
            } else {
                Food newFood = new Food();
                newFood.setName(addFoodRequest.getName());
                newFood.setCarbs(addFoodRequest.getCarbs());
                newFood.setFat(addFoodRequest.getFat());
                newFood.setProtein(addFoodRequest.getProtein());

                int insertedRows = foodRepository.insertFood(userInfo.getUserId(), newFood);
                if (insertedRows == 1 && addFoodRequest.getPortions() != null && !addFoodRequest.getPortions().isEmpty()) {
                    Food addedFood = foodRepository.getFood(userInfo.getUserId(), addFoodRequest.getName());
                    for (PortionDto portionDto : addFoodRequest.getPortions()) {
                        csl.database.model.Portion newPortion = new csl.database.model.Portion();
                        newPortion.setDescription(portionDto.getDescription());
                        newPortion.setGrams(portionDto.getGrams());

                        portionRepository.addPortion(addedFood.getId(), newPortion);
                    }
                }

                return ResponseEntity.status(HttpStatus.CREATED).build();
            }
        }
    }

    private FoodDto createFoodDto(Food food, boolean withPortions) {
        FoodDto foodDto = mapFoodToFoodDto(food);

        if (withPortions) {
            List<Portion> foodPortions = portionRepository.getPortions(food.getId());
            for (Portion portion : foodPortions) {
                PortionDto portionDto = mapPortionToPortionDto(portion, food);
                foodDto.addPortion(portionDto);
            }
        }
        return foodDto;
    }

    static PortionDto mapPortionToPortionDto(Portion portion, Food food) {
        PortionDto currDto = new PortionDto();
        currDto.setDescription(portion.getDescription());
        currDto.setGrams(portion.getGrams());
        currDto.setId(portion.getId());
        Macro calculatedMacros = calculateMacro(food, portion);
        currDto.setMacros(calculatedMacros);
        return currDto;
    }

    static FoodDto mapFoodToFoodDto(Food food) {
        FoodDto foodDto = new FoodDto();
        foodDto.setName(food.getName());
        foodDto.setId(food.getId());
        foodDto.setProtein(food.getProtein());
        foodDto.setCarbs(food.getCarbs());
        foodDto.setFat(food.getFat());
        return foodDto;
    }

    // Naar een util brengen:
    static Macro calculateMacro(Food food, csl.database.model.Portion portion) {
        Macro calculatedMacros = new Macro();
        // FoodDto has been entered for 100g
        calculatedMacros.setCarbs(food.getCarbs() / 100 * portion.getGrams());
        calculatedMacros.setProtein(food.getProtein() / 100 * portion.getGrams());
        calculatedMacros.setFat(food.getFat() / 100 * portion.getGrams());

        return calculatedMacros;
    }

    static Macro calculateMacro(FoodDto food, PortionDto portion) {
        Macro calculatedMacros = new Macro();
        // FoodDto has been entered for 100g
        calculatedMacros.setCarbs(food.getCarbs() / 100 * portion.getGrams());
        calculatedMacros.setProtein(food.getProtein() / 100 * portion.getGrams());
        calculatedMacros.setFat(food.getFat() / 100 * portion.getGrams());

        return calculatedMacros;
    }
}
