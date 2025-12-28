package slt.mapper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.*;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import slt.database.FoodRepository;
import slt.database.PortionRepository;
import slt.database.entities.*;
import slt.dto.*;
import slt.util.MacroUtils;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class MyModelMapper {

    @Autowired
    FoodRepository foodRepository;

    @Autowired
    PortionRepository portionRepository;

    @Getter
    private final ModelMapper configuredMapper;

    public MyModelMapper() {
        log.debug("Creating Macrolog ModelMapper");
        org.modelmapper.ModelMapper modelMapper = new org.modelmapper.ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // Basic date converters
        addSqlDateLocalDate(modelMapper);
        addLocalDateSqlDate(modelMapper);

        // Meals + Ingredients:
        addDishDtoDish(modelMapper);
        addDishDishDto(modelMapper);
//        addIngredientIngredientDto(modelMapper);
//        addIngredientDtoIngredient(modelMapper);
        addLogEntryEntryDto(modelMapper);
        addEntryDtoEntry(modelMapper);

        // Mealplans
        addMealplanToMealplanDto(modelMapper);
//        addMealplanDtoToMealplan(modelMapper);
//        addMealtimeToMealtimeDto(modelMapper);
//        addMealtimeDtoToMealtime(modelMapper);

        // Weight
        final PropertyMap<WeightDto, Weight> weightDtoMapper = getWeightDtoWeightPropertyMap();
        final PropertyMap<Weight, WeightDto> weightEntityMapper = getWeightWeightDtoPropertyMap();
        modelMapper.addMappings(weightDtoMapper);
        modelMapper.addMappings(weightEntityMapper);

        // Setting
        final PropertyMap<SettingDto, Setting> settingDtoMapper = getSettingDtoSettingPropertyMap();
        modelMapper.addMappings(settingDtoMapper);

        // Portion
        final PropertyMap<Portion, PortionDto> portionPortionDtoPropertyMap = getPortionPortionDtoPropertyMap();
        modelMapper.addMappings(portionPortionDtoPropertyMap);

        // Food
        final PropertyMap<FoodDto, Food> foodDtoFoodPropertyMap = getFoodDtoFoodPropertyMap();
        final PropertyMap<Food, FoodDto> foodFoodDtoPropertyMap = getFoodFoodDtoPropertyMap();
        modelMapper.addMappings(foodDtoFoodPropertyMap);
        modelMapper.addMappings(foodFoodDtoPropertyMap);

        // LogActivity
        final PropertyMap<ActivityDto, Activity> logActivityDtoLogActivityPropertyMap = geLogActivityDtoLogActivityPropertyMap();
        modelMapper.addMappings(logActivityDtoLogActivityPropertyMap);

        this.configuredMapper = modelMapper;
    }

    private void addMealplanToMealplanDto(final ModelMapper modelMapper) {
//        modelMapper.createTypeMap(Mealplan.class, MealplanDto.class)
//                .setPostConverter()
    }

    private void addDishDtoDish(ModelMapper modelMapper) {
        modelMapper.createTypeMap(DishDto.class, Dish.class)
                .setPostConverter(mappingContext -> {
                    if (mappingContext.getDestination().getIngredients() != null) {
                        for (Ingredient ingredient : mappingContext.getDestination().getIngredients()) {
                            ingredient.setDish(mappingContext.getDestination());
                        }
                    }
                    return mappingContext.getDestination();
                });
    }

    private void addDishDishDto(ModelMapper modelMapper) {
        modelMapper.createTypeMap(Dish.class, DishDto.class)
                .setPostConverter(mappingContext -> {
                    if (mappingContext.getSource().getIngredients() == null) {
                        mappingContext.getDestination().setIngredients(new ArrayList<>());
                    }

                    MacroDto macrosCalculated = new MacroDto(0.0, 0.0, 0.0, 0);
                    for (IngredientDto ingredientDto : mappingContext.getDestination().getIngredients()) {

                        MacroDto macroDto;
                        if (ingredientDto.getPortion() != null) {
                            macroDto = calculateMacro(ingredientDto.getFood(), ingredientDto.getPortion());
                        } else {
                            macroDto = new MacroDto(ingredientDto.getFood().getProtein(),
                                    ingredientDto.getFood().getFat(),
                                    ingredientDto.getFood().getCarbs(),
                                    MacroUtils.calculateCalories(ingredientDto.getFood())
                                    );
                        }
                        macroDto = MacroUtils.multiply(macroDto, ingredientDto.getMultiplier());
                        macrosCalculated = MacroUtils.add(macrosCalculated, macroDto);
                    }

                    mappingContext.getDestination().setMacrosCalculated(macrosCalculated);
                    return mappingContext.getDestination();
                });
    }

    private void addIngredientDtoIngredient(ModelMapper modelMapper) {
        modelMapper.createTypeMap(IngredientDto.class, Ingredient.class)
                .setPostConverter(mappingContext -> {
                    if (mappingContext.getSource().getPortion() != null) {
                        Long portionId = mappingContext.getSource().getPortion().getId();
//                        mappingContext.getDestination().setPortionId(portionId);
                    }

                    FoodDto food = mappingContext.getSource().getFood();
//                    mappingContext.getDestination().setFoodId(food.getId());
                    // Todo: check if food actually exists for user.
                    return mappingContext.getDestination();
                });
    }

    private void addIngredientIngredientDto(ModelMapper modelMapper) {
        modelMapper.createTypeMap(Ingredient.class, IngredientDto.class)
                .setPostConverter(mappingContext -> {
                    mappingContext.getDestination().setId(mappingContext.getSource().getId());
//                    Long foodId = mappingContext.getSource().getFoodId();
                    Long userId = mappingContext.getSource().getDish().getUserId();
//                    Food foodById = foodRepository.getFoodById(userId, foodId);
//                    FoodDto mappedFoodDto = modelMapper.map(foodById, FoodDto.class);
//
//                    for (Portion portion : portionRepository.getPortions(foodId)) {
//                        PortionDto mappedPortion = modelMapper.map(portion, PortionDto.class);
//                        mappedPortion.setMacros(calculateMacro(mappedFoodDto, mappedPortion));
//                        mappedFoodDto.addPortion(mappedPortion);
//                    }
//                    mappingContext.getDestination().setPortion(mappedFoodDto.getPortions()
//                            .stream()
//                            .filter(p -> p.getId().equals(mappingContext.getSource().getPortionId()))
//                            .findFirst()
//                            .orElse(null));
//
//
//                    mappingContext.getDestination().setFood(mappedFoodDto);

                    return mappingContext.getDestination();
                });
    }

    private static MacroDto calculateMacro(FoodDto food, PortionDto portion) {
        MacroDto calculatedMacros = new MacroDto();
        // FoodDto has been entered for 100g
        calculatedMacros.setCarbs(food.getCarbs() / 100 * portion.getGrams());
        calculatedMacros.setProtein(food.getProtein() / 100 * portion.getGrams());
        calculatedMacros.setFat(food.getFat() / 100 * portion.getGrams());

        return calculatedMacros;
    }

    private void addLogEntryEntryDto(ModelMapper modelMapper) {
        modelMapper.createTypeMap(LogEntry.class, EntryDto.class)
                .setPostConverter(mappingContext -> {
                    Long foodId = mappingContext.getSource().getFoodId();
                    Long userId = mappingContext.getSource().getUserId();
                    final var optionalFood = foodRepository.getFoodById(userId, foodId);
                    assert optionalFood.isPresent();
                    FoodDto mappedFoodDto = modelMapper.map(optionalFood.get(), FoodDto.class);

                    List<Portion> foodPortions = portionRepository.getPortions(foodId);
                    for (Portion portion : foodPortions) {
                        PortionDto currDto = modelMapper.map(portion, PortionDto.class);
                        currDto.setMacros(calculateMacro(mappedFoodDto, currDto));
                        mappedFoodDto.addPortion(currDto);
                    }
                    mappingContext.getDestination().setFood(mappedFoodDto);

                    Long selectedPortionId = mappingContext.getSource().getPortionId();
                    if (selectedPortionId != null) {
                        Optional<PortionDto> first = mappedFoodDto.getPortions().stream().filter(p -> p.getId().equals(selectedPortionId)).findFirst();
                        if (first.isPresent()) {
                            mappingContext.getDestination().setPortion(first.get());
                        } else {
                            log.error("Unknown portion {} with food {}", selectedPortionId, optionalFood.get());
                        }
                    }

                    MacroDto macrosCalculated = new MacroDto();
                    Double multiplier = mappingContext.getSource().getMultiplier();
                    if (selectedPortionId != null) {
                        macrosCalculated = mappingContext.getDestination().getPortion().getMacros().createCopy();
                        macrosCalculated = MacroUtils.multiply(macrosCalculated, multiplier);

                    } else {
                        macrosCalculated.setCarbs(multiplier * mappedFoodDto.getCarbs());
                        macrosCalculated.setFat(multiplier * mappedFoodDto.getFat());
                        macrosCalculated.setProtein(multiplier * mappedFoodDto.getProtein());
                        macrosCalculated.setCalories(MacroUtils.calculateCalories(macrosCalculated));
                    }
                    mappingContext.getDestination().setMacrosCalculated(macrosCalculated);

                    return mappingContext.getDestination();
                });
    }

    private void addEntryDtoEntry(ModelMapper modelMapper) {
        modelMapper.createTypeMap(EntryDto.class, LogEntry.class)
                .setPostConverter(mappingContext -> {
                    EntryDto dto = mappingContext.getSource();
                    mappingContext.getDestination().setId(dto.getId());
                    mappingContext.getDestination().setFoodId(dto.getFood().getId());
                    mappingContext.getDestination().setPortionId(dto.getPortion() != null ? dto.getPortion().getId() : null);
                    mappingContext.getDestination().setMeal(dto.getMeal().toString());
                    mappingContext.getDestination().setMultiplier(dto.getMultiplier());
                    mappingContext.getDestination().setDay(modelMapper.map(dto.getDay(), Date.class));

                    return mappingContext.getDestination();
                });
    }

    private void addLocalDateSqlDate(ModelMapper modelMapper) {
        Provider<LocalDate> localDateProvider = new AbstractProvider<>() {
            @Override
            protected LocalDate get() {
                return LocalDate.now();
            }
        };
        Converter<Date, LocalDate> toLocalDateConverter = new AbstractConverter<>() {
            protected LocalDate convert(Date source) {
                return source.toLocalDate();
            }
        };

        modelMapper.createTypeMap(Date.class, LocalDate.class);
        modelMapper.addConverter(toLocalDateConverter);
        modelMapper.getTypeMap(Date.class, LocalDate.class).setProvider(localDateProvider);
    }

    private void addSqlDateLocalDate(ModelMapper modelMapper) {
        Provider<Date> sqlDateProvider = new AbstractProvider<>() {
            @Override
            protected Date get() {
                return Date.valueOf(LocalDate.now());
            }
        };

        Converter<LocalDate, Date> toSqlDateConverter = new AbstractConverter<>() {
            protected Date convert(LocalDate source) {
                return Date.valueOf(source);
            }
        };

        modelMapper.createTypeMap(LocalDate.class, Date.class);
        modelMapper.addConverter(toSqlDateConverter);
        modelMapper.getTypeMap(LocalDate.class, Date.class).setProvider(sqlDateProvider);
    }

    private PropertyMap<WeightDto, Weight> getWeightDtoWeightPropertyMap() {
        return new PropertyMap<>() {
            @Override
            protected void configure() {
                map().setWeight(source.getWeight());
                skip().setUserId(null);
            }
        };
    }

    private PropertyMap<Weight, WeightDto> getWeightWeightDtoPropertyMap() {
        return new PropertyMap<>() {
            @Override
            protected void configure() {
                map().setWeight(source.getWeight());
            }
        };
    }

    private PropertyMap<SettingDto, Setting> getSettingDtoSettingPropertyMap() {
        return new PropertyMap<>() {
            @Override
            protected void configure() {
                skip().setUserId(null);
            }
        };
    }

    private PropertyMap<Portion, PortionDto> getPortionPortionDtoPropertyMap() {
        return new PropertyMap<>() {
            @Override
            protected void configure() {
                skip().setMacros(null);
            }
        };
    }

    private PropertyMap<FoodDto, Food> getFoodDtoFoodPropertyMap() {
        return new PropertyMap<>() {
            @Override
            protected void configure() {
                skip().setUserId(null);
            }
        };
    }

    private PropertyMap<Food, FoodDto> getFoodFoodDtoPropertyMap() {
        return new PropertyMap<>() {
            @Override
            protected void configure() {
                skip().setPortions(new ArrayList<>());
            }
        };
    }

    private PropertyMap<ActivityDto, Activity> geLogActivityDtoLogActivityPropertyMap() {
        return new PropertyMap<>() {
            @Override
            protected void configure() {
                skip().setUserId(null);
                skip().setStatus(null);
            }
        };
    }

}
