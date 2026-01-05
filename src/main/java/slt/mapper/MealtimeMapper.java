package slt.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Ingredient;
import slt.database.entities.Mealtime;
import slt.dto.MealtimeDto;

@Mapper(uses = IngredientMapper.class,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface MealtimeMapper {

    MealtimeMapper INSTANCE = Mappers.getMapper(MealtimeMapper.class);

    MealtimeDto map(Mealtime mealtime);

    @Mapping(target = "mealplan", ignore = true)
    Mealtime map(final MealtimeDto dto);

    @AfterMapping
    default void addParentToChildren(final @MappingTarget Mealtime mealtime) {
        if (mealtime.getIngredients() != null) {
            for (Ingredient ingredient : mealtime.getIngredients()) {
                ingredient.setMealtime(mealtime);
            }
        }
    }

}
