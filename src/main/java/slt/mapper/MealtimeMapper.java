package slt.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Ingredient;
import slt.database.entities.Mealtime;
import slt.dto.MealtimeDto;
import slt.dto.requests.MealtimeRequest;

@Mapper(uses = IngredientMapper.class)
public interface MealtimeMapper {

    MealtimeMapper INSTANCE = Mappers.getMapper(MealtimeMapper.class);

    MealtimeDto map(Mealtime mealtime);

    @Mapping(target = "mealplan", ignore = true)
    @Mapping(source = "request.ingredients", target = "ingredients")
    Mealtime map(final MealtimeRequest request);

    @AfterMapping
    default void addParentToChildren(final MealtimeRequest request, final @MappingTarget Mealtime mealtime) {
        if (mealtime.getIngredients() != null) {
            for (Ingredient ingredient : mealtime.getIngredients()) {
                ingredient.setMealtime(mealtime);
            }
        }
    }

}
