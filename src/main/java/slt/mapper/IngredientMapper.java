package slt.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Ingredient;
import slt.database.entities.Portion;
import slt.dto.IngredientDto;

@Mapper
public interface IngredientMapper {

    MealtimeMapper INSTANCE = Mappers.getMapper(MealtimeMapper.class);

    @Mapping(target = "dish", ignore = true)
    @Mapping(target = "mealtime", ignore = true)
    @Mapping(source = "portion.id", target = "portion", qualifiedByName = "portion")
    Ingredient map(final IngredientDto dto);

    @Named("portion")
    default Portion portionOrNull(final Long portionId) {
        if (portionId == null) return null;
        return Portion.builder().id(portionId).build();
    }
}
