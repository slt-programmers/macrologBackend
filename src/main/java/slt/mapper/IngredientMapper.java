package slt.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Ingredient;
import slt.database.entities.Portion;
import slt.dto.requests.IngredientRequest;

@Mapper
public interface IngredientMapper {

    MealtimeMapper INSTANCE = Mappers.getMapper(MealtimeMapper.class);

    @Mapping(target = "dish", ignore = true)
    @Mapping(target = "mealtime", ignore = true)
    @Mapping(source = "request.foodId", target = "food.id")
    @Mapping(source = "request.portionId", target = "portion", qualifiedByName = "portion")
    Ingredient map(final IngredientRequest request);

    @Named("portion")
    default Portion portionOrNull(final Long portionId) {
        if (portionId == null) return null;
        return Portion.builder().id(portionId).build();
    }
}
