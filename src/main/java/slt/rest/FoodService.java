package slt.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import slt.mapper.MyModelMapper;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/food")
public class FoodService {

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private PortionRepository portionRepository;

    @Autowired
    private MyModelMapper myModelMapper;

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

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addFood(@RequestBody FoodDto foodDto) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        if (foodDto.getId() != null) {
            // Update request
            updateFoodRequest(foodDto, userInfo);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            Food food = foodRepository.getFood(userInfo.getUserId(), foodDto.getName());
            if (food != null) {
                log.error("This food is already in your database");
                return ResponseEntity.badRequest().build();
            } else {
                return createNewFood(foodDto, userInfo);
            }
        }
    }

    private ResponseEntity<Void> createNewFood(@RequestBody final FoodDto foodDto, final UserInfo userInfo) {
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
        MacroDto calculatedMacros = calculateMacro(food, portion);
        currDto.setMacros(calculatedMacros);
        return currDto;
    }

    //TODO Naar een util brengen
    private static MacroDto calculateMacro(Food food, Portion portion) {
        MacroDto calculatedMacros = new MacroDto();
        // FoodDto has been entered for 100g
        calculatedMacros.setCarbs(food.getCarbs() / 100 * portion.getGrams());
        calculatedMacros.setProtein(food.getProtein() / 100 * portion.getGrams());
        calculatedMacros.setFat(food.getFat() / 100 * portion.getGrams());

        return calculatedMacros;
    }

    static MacroDto calculateMacro(FoodDto food, PortionDto portion) {
        MacroDto calculatedMacros = new MacroDto();
        // FoodDto has been entered for 100g
        calculatedMacros.setCarbs(food.getCarbs() / 100 * portion.getGrams());
        calculatedMacros.setProtein(food.getProtein() / 100 * portion.getGrams());
        calculatedMacros.setFat(food.getFat() / 100 * portion.getGrams());

        return calculatedMacros;
    }
}
