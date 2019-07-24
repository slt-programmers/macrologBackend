package slt.dto;


import lombok.extern.slf4j.Slf4j;
import org.modelmapper.*;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import slt.database.FoodRepository;
import slt.database.PortionRepository;
import slt.database.entities.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class MyModelMapper {

    @Autowired
    FoodRepository foodRepository;

    @Autowired
    PortionRepository portionRepository;

    public org.modelmapper.ModelMapper getConfiguredMapper() {
        org.modelmapper.ModelMapper modelMapper = new org.modelmapper.ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // Basic date converters
        addSqlDateLocalDate(modelMapper);
        addLocalDateSqlDate(modelMapper);

        // Meals + Ingredients:
        addAddMealRequestMeal(modelMapper);
        addMealDtoMeal(modelMapper);
        addIngredientIngredientDto(modelMapper);
        addIngredientDtoIngredient(modelMapper);
        addLogEntryLogEntryDto(modelMapper);

        // Weight
        PropertyMap<WeightDto, Weight> weightDtoMapper = getWeightDtoWeightPropertyMap();
        PropertyMap<Weight, WeightDto> weightEntityMapper = getWeightWeightDtoPropertyMap();

        modelMapper.addMappings(weightDtoMapper);
        modelMapper.addMappings(weightEntityMapper);
        return modelMapper;
    }

    private void addAddMealRequestMeal(ModelMapper modelMapper) {
        modelMapper.createTypeMap(AddMealRequest.class, Meal.class)
                .setPostConverter(mappingContext -> {
                    if (mappingContext.getDestination().getIngredients() != null) {
                        for (Ingredient ingredient : mappingContext.getDestination().getIngredients()) {
                            ingredient.setMeal(mappingContext.getDestination());
                        }
                    }
                    return mappingContext.getDestination();
                });
    }

    private void addMealDtoMeal(ModelMapper modelMapper) {
        modelMapper.createTypeMap(MealDto.class, Meal.class)
                .setPostConverter(mappingContext -> {
                    if (mappingContext.getDestination().getIngredients() != null) {
                        for (Ingredient ingredient : mappingContext.getDestination().getIngredients()) {
                            ingredient.setMeal(mappingContext.getDestination());
                        }
                    }
                    return mappingContext.getDestination();
                });
    }

    private void addIngredientDtoIngredient(ModelMapper modelMapper) {
        modelMapper.createTypeMap(IngredientDto.class, Ingredient.class)
                .setPostConverter(mappingContext -> {
                    FoodDto food = mappingContext.getSource().getFood();
                    mappingContext.getDestination().setFoodId(food.getId());
                    // Todo: check if food actually exists for user.
                    return mappingContext.getDestination();
                });
    }

    private void addIngredientIngredientDto(ModelMapper modelMapper) {
        modelMapper.createTypeMap(Ingredient.class, IngredientDto.class)
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
    }
    static Macro calculateMacro(FoodDto food, PortionDto portion) {
        Macro calculatedMacros = new Macro();
        // FoodDto has been entered for 100g
        calculatedMacros.setCarbs(food.getCarbs() / 100 * portion.getGrams());
        calculatedMacros.setProtein(food.getProtein() / 100 * portion.getGrams());
        calculatedMacros.setFat(food.getFat() / 100 * portion.getGrams());

        return calculatedMacros;
    }
    private void addLogEntryLogEntryDto(ModelMapper modelMapper) {
        modelMapper.createTypeMap(LogEntry.class, LogEntryDto.class)
                .setPostConverter(mappingContext -> {

                    Long foodId = mappingContext.getSource().getFoodId();
                    Integer userId = mappingContext.getSource().getUserId();
                    Food foodById = foodRepository.getFoodById(userId, foodId);
                    FoodDto mappedFoodDto = modelMapper.map(foodById, FoodDto.class);

                    List<Portion> foodPortions = portionRepository.getPortions(foodId);
                    for (Portion portion : foodPortions) {
                        PortionDto currDto = modelMapper.map(portion,PortionDto.class);
                        currDto.setMacros(calculateMacro(mappedFoodDto,currDto));
                        mappedFoodDto.addPortion(currDto);
                    }
                    mappingContext.getDestination().setFood(mappedFoodDto);

                    Long selectedPortionId = mappingContext.getSource().getPortionId();
                    if (selectedPortionId != null){
                        Optional<PortionDto> first = mappedFoodDto.getPortions().stream().filter(p -> p.getId().equals(selectedPortionId)).findFirst();
                        if (first.isPresent()){
                            mappingContext.getDestination().setPortion(first.get());
                        } else {
                            log.error("Unknown portion {} with food {}",selectedPortionId,foodById);
                        }
                    }

                    Macro macrosCalculated = new Macro();
                    Double multiplier = mappingContext.getSource().getMultiplier();
                    if (selectedPortionId != null) {
                        macrosCalculated = mappingContext.getDestination().getPortion().getMacros().createCopy();
                        macrosCalculated.multiply(multiplier);

                    } else {
                        macrosCalculated.setCarbs(multiplier * mappedFoodDto.getCarbs());
                        macrosCalculated.setFat(multiplier * mappedFoodDto.getFat());
                        macrosCalculated.setProtein(multiplier * mappedFoodDto.getProtein());
                    }
                    mappingContext.getDestination().setMacrosCalculated(macrosCalculated);

                    return mappingContext.getDestination();
                });
    }

    private void addLocalDateSqlDate(ModelMapper modelMapper) {
        Provider<LocalDate> localDateProvider = new AbstractProvider<LocalDate>() {
            @Override
            protected LocalDate get() {
                return LocalDate.now();
            }
        };
        Converter<Date, LocalDate> toLocalDateConverter = new AbstractConverter<Date, LocalDate>() {
            protected LocalDate convert(Date source) {
                return source.toLocalDate();
            }
        };

        modelMapper.createTypeMap(Date.class, LocalDate.class);
        modelMapper.addConverter(toLocalDateConverter);
        modelMapper.getTypeMap(Date.class, LocalDate.class).setProvider(localDateProvider);
    }

    private void addSqlDateLocalDate(ModelMapper modelMapper) {
        Provider<Date> sqlDateProvider = new AbstractProvider<Date>() {
            @Override
            protected Date get() {
                return Date.valueOf(LocalDate.now());
            }
        };

        Converter<LocalDate, Date> toSqlDateConverter = new AbstractConverter<LocalDate, Date>() {
            protected Date convert(LocalDate source) {
                return Date.valueOf(source);
            }
        };

        modelMapper.createTypeMap(LocalDate.class, Date.class);
        modelMapper.addConverter(toSqlDateConverter);
        modelMapper.getTypeMap(LocalDate.class, Date.class).setProvider(sqlDateProvider);
    }

    private PropertyMap<WeightDto, Weight> getWeightDtoWeightPropertyMap() {
        return new PropertyMap<WeightDto, Weight>() {
            @Override
            protected void configure() {
                map().setValue(source.getWeight());
                skip().setUserId(null);
            }
        };
    }

    private PropertyMap<Weight, WeightDto> getWeightWeightDtoPropertyMap() {
        return new PropertyMap<Weight, WeightDto>() {
            @Override
            protected void configure() {
                map().setWeight(source.getValue());
            }
        };
    }
}
