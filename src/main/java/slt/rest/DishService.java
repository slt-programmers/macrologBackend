package slt.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.database.FoodRepository;
import slt.database.DishRepository;
import slt.database.PortionRepository;
import slt.database.entities.Dish;
import slt.dto.AddDishRequest;
import slt.dto.DishDto;
import slt.dto.MyModelMapper;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dishes")
@Api(value = "dishes")
public class DishService {

    @Autowired
    private DishRepository dishRepository;
    @Autowired
    private FoodRepository foodRepository;
    @Autowired
    private PortionRepository portionRepository;

    @Autowired
    private MyModelMapper myModelMapper;

    @ApiOperation(value = "Retrieve all dishes")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DishDto>> getAllDishes() {

        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        List<Dish> allDishes = dishRepository.getAllDishes(userInfo.getUserId());

        List<DishDto> allDishedDto = allDishes.stream()
                .map(dish -> myModelMapper.getConfiguredMapper().map(dish, DishDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(allDishedDto);
    }

    @ApiOperation(value = "Save dish")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DishDto> storeDish(@RequestBody AddDishRequest dishDto) {

        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        Dish map = myModelMapper.getConfiguredMapper().map(dishDto, Dish.class);

        if (dishRepository.findByName(userInfo.getUserId(), dishDto.getName()) != null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Dish dish = dishRepository.saveDish(userInfo.getUserId(), map);

        return ResponseEntity.status(HttpStatus.CREATED).body(myModelMapper.getConfiguredMapper().map(dish,DishDto.class));
    }

    @ApiOperation(value = "Delete dish")
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteDish(@PathVariable("id") Long dishId) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        dishRepository.deleteDish(userInfo.getUserId(), dishId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
