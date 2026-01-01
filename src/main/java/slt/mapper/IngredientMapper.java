package slt.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Ingredient;
import slt.database.entities.Portion;
import slt.dto.IngredientDto;
import slt.dto.PortionDto;

@Mapper
public interface IngredientMapper {

    MealtimeMapper INSTANCE = Mappers.getMapper(MealtimeMapper.class);

    @Mapping(target = "dish", ignore = true)
    @Mapping(target = "mealtime", ignore = true)
    @Mapping(source = "portion", target = "portion", qualifiedByName = "portionNullable")
    Ingredient map(final IngredientDto dto);

    @Named("portionNullable")
    default Portion portionNullable(final PortionDto portionDto) {
        if (portionDto == null) return null;
        return Portion.builder().id(portionDto.getId()).build();
    }
}
