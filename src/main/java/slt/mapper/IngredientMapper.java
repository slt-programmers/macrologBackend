package slt.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Ingredient;
import slt.dto.IngredientDto;

@Mapper(uses = {FoodMapper.class, PortionMapper.class})
public interface IngredientMapper {

    MealtimeMapper INSTANCE = Mappers.getMapper(MealtimeMapper.class);

    @Mapping(target = "dish", ignore = true)
    @Mapping(target = "mealtime", ignore = true)
    Ingredient map(final IngredientDto dto);

    IngredientDto map(final Ingredient ingredient);

}
