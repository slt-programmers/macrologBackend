package slt.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Dish;
import slt.database.entities.Ingredient;
import slt.dto.DishDto;

import java.util.List;

@Mapper(uses = IngredientMapper.class,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface DishMapper {

    DishMapper INSTANCE = Mappers.getMapper(DishMapper.class);

    DishDto map(final Dish dish);

    List<DishDto> map(final List<Dish> dishes);

    Dish map(final DishDto dishDto, final Long userId);

    @AfterMapping
    default void setParentInChildren(@MappingTarget Dish dish) {
        if (dish.getIngredients() != null) {
            for (Ingredient ingredient : dish.getIngredients()) {
                ingredient.setDish(dish);
            }
        }
    }
}
