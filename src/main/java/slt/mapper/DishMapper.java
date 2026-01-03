package slt.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Dish;
import slt.dto.DishDto;
import slt.dto.MacroDto;
import slt.util.MacroUtils;

import java.util.List;

@Mapper(uses = IngredientMapper.class,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface DishMapper {

    DishMapper INSTANCE = Mappers.getMapper(DishMapper.class);

    @Mapping(source = "dish", target = "macrosCalculated", qualifiedByName = "macrosCalculated")
    DishDto map(final Dish dish);

    @Named("macrosCalculated")
    default MacroDto macrosCalculated(final Dish dish) {
        var macroDto = MacroDto.builder().protein(0D).fat(0D).carbs(0D).calories(0).build();
        for (final var ingredient : dish.getIngredients()) {
            final var ingredientMacros = MacroUtils.calculateMacro(ingredient.getFood(), ingredient.getPortion(), ingredient.getMultiplier());
            macroDto = MacroUtils.add(macroDto, ingredientMacros);
        }
        return macroDto;
    }

    List<DishDto> map(final List<Dish> dishes);

    Dish map(final DishDto dishDto, final Long userId);

    @AfterMapping
    default void setParentInChildren(@MappingTarget Dish dish) {
        for (final var ingredient : dish.getIngredients()) {
            ingredient.setDish(dish);
        }
    }
}
