package slt.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Food;
import slt.dto.FoodDto;
import slt.util.MacroUtils;

import java.util.List;

@Mapper(uses = PortionMapper.class,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface FoodMapper {

    FoodMapper INSTANCE = Mappers.getMapper(FoodMapper.class);

    FoodDto map(final Food food);

    List<FoodDto> map(final List<Food> foodList);

    Food map(final FoodDto foodDto);

    @AfterMapping
    default void addMacrosToPortions(final @MappingTarget FoodDto food) {
        final var portions = food.getPortions();
        if (portions != null) {
            for (final var portion : portions) {
                portion.setMacros(MacroUtils.calculateMacro(food, portion));
            }
        }
    }

    @AfterMapping
    default void addParentToChildren(final @MappingTarget Food food) {
        final var portions = food.getPortions();
        if (portions != null) {
            for (final var portion : portions) {
                portion.setFood(food);
            }
        }
    }
}
