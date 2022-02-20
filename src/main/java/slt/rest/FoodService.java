package slt.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.database.FoodRepository;
import slt.database.PortionRepository;
import slt.database.entities.Food;
import slt.database.entities.Portion;
import slt.dto.*;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@RestController
@AllArgsConstructor
@RequestMapping("/food")
@Api(value = "food")
public class FoodService {

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private PortionRepository portionRepository;

    @Autowired
    private MyModelMapper myModelMapper;

    @ApiOperation(value = "Retrieve all stored foods")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FoodDto>> getAllFood() {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<Food> allFood = foodRepository.getAllFood(userInfo.getUserId());
        List<FoodDto> allFoodDtos = new ArrayList<>();
        for (Food food : allFood) {
            allFoodDtos.add(createFoodDto(food));
        }
        allFoodDtos.sort(Comparator.comparing(FoodDto::getName));
        return ResponseEntity.ok(allFoodDtos);
    }

    @ApiOperation(value = "Retrieve information about specific food")
    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FoodDto> getFoodById(@PathVariable("id") Long id) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        Food food = foodRepository.getFoodById(userInfo.getUserId(), id);
        if (food == null) {
            return ResponseEntity.noContent().build();
        } else {
            FoodDto foodDto = createFoodDto(food);
            return ResponseEntity.ok(foodDto);
        }
    }

    @ApiOperation(value = "Store new food with supplied macro per 100 grams")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addFood(@RequestBody FoodDto foodDto) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        if (foodDto.getId() != null) {
            // Update request
            updateFoodRequest(foodDto, userInfo);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            Food food = foodRepository.getFood(userInfo.getUserId(), foodDto.getName());
            if (food != null) {
                String errorMessage = "This food is already in your database";
                return ResponseEntity.badRequest().body(errorMessage);
            } else {
                return createNewFood(foodDto, userInfo);
            }
        }
    }

    private ResponseEntity createNewFood(@RequestBody FoodDto foodDto, UserInfo userInfo) {
        Food newFood = new Food();
        newFood.setName(foodDto.getName());
        newFood.setCarbs(foodDto.getCarbs());
        newFood.setFat(foodDto.getFat());
        newFood.setProtein(foodDto.getProtein());

        Food insertedFood = foodRepository.saveFood(userInfo.getUserId(), newFood);
        if (foodDto.getPortions() != null && !foodDto.getPortions().isEmpty()) {
            for (PortionDto portionDto : foodDto.getPortions()) {
                Portion newPortion = new Portion();
                newPortion.setDescription(portionDto.getDescription());
                newPortion.setGrams(portionDto.getGrams());
                portionRepository.savePortion(insertedFood.getId(), newPortion);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private void updateFoodRequest(@RequestBody FoodDto foodDto, UserInfo userInfo) {
        Food newFood = new Food();
        newFood.setId(foodDto.getId());
        newFood.setName(foodDto.getName());
        newFood.setCarbs(foodDto.getCarbs());
        newFood.setFat(foodDto.getFat());
        newFood.setProtein(foodDto.getProtein());

        foodRepository.saveFood(userInfo.getUserId(), newFood);

        // remove portions not supported yet.
        for (PortionDto portionDto : foodDto.getPortions()) {
            Long id = portionDto.getId();
            if (id != null) {
                // update portion
                Portion newPortion = new Portion();
                newPortion.setId(portionDto.getId());
                newPortion.setDescription(portionDto.getDescription());
                newPortion.setGrams(portionDto.getGrams());
                portionRepository.savePortion(newFood.getId(), newPortion);

            } else {
                // add portion
                Portion newPortion = new Portion();
                newPortion.setDescription(portionDto.getDescription());
                newPortion.setGrams(portionDto.getGrams());
                portionRepository.savePortion(newFood.getId(), newPortion);
            }
        }
    }

    private FoodDto createFoodDto(Food food) {
        FoodDto foodDto = myModelMapper.getConfiguredMapper().map(food, FoodDto.class);
        List<Portion> foodPortions = portionRepository.getPortions(food.getId());
        for (Portion portion : foodPortions) {
            PortionDto portionDto = mapPortionToPortionDto(portion, food);
            foodDto.addPortion(portionDto);
        }
        return foodDto;
    }

    private PortionDto mapPortionToPortionDto(Portion portion, Food food) {
        PortionDto currDto = myModelMapper.getConfiguredMapper().map(portion, PortionDto.class);
        Macro calculatedMacros = calculateMacro(food, portion);
        currDto.setMacros(calculatedMacros);
        return currDto;
    }

    //TODO Naar een util brengen
    private static Macro calculateMacro(Food food, Portion portion) {
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
