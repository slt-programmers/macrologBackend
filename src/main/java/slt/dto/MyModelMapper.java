package slt.dto;


import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import slt.database.FoodRepository;
import slt.database.PortionRepository;
import slt.database.entities.Food;
import slt.database.entities.Ingredient;
import slt.database.entities.Meal;
import slt.database.entities.Portion;

@Component
public class MyModelMapper {

    @Autowired
    FoodRepository foodRepository;

    @Autowired
    PortionRepository portionRepository;

    public org.modelmapper.ModelMapper getConfiguredMapper() {
        org.modelmapper.ModelMapper modelMapper = new org.modelmapper.ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.createTypeMap(AddMealRequest.class, Meal.class)
                .setPostConverter(mappingContext -> {
                    if (mappingContext.getDestination().getIngredients() != null) {
                        for (Ingredient ingredient : mappingContext.getDestination().getIngredients()) {
                            ingredient.setMeal(mappingContext.getDestination());
                        }
                    }
                    return mappingContext.getDestination();
                });

        modelMapper.createTypeMap(Ingredient.class,IngredientDto.class)
                .setPostConverter(mappingContext -> {
                    Long foodId = mappingContext.getSource().getFoodId();
                    Integer userId = mappingContext.getSource().getMeal().getUserId();
                    Food foodById = foodRepository.getFoodById(userId, foodId);
                    FoodDto mappedFoodDto = modelMapper.map(foodById, FoodDto.class);

                    for (Portion portion : portionRepository.getPortions(foodId)) {
                        PortionDto mappedPortion = modelMapper.map(portion, PortionDto.class);
                        mappedFoodDto.addPortion(mappedPortion);
                    }

                    mappingContext.getDestination().setFood(mappedFoodDto);

                    return mappingContext.getDestination();
                });


        return modelMapper;
    }

}
