package slt.mapper;


import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Weight;
import slt.dto.WeightDto;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Mapper(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface WeightMapper {

    WeightMapper INSTANCE = Mappers.getMapper(WeightMapper.class);

    WeightDto map(final Weight weight);

    List<WeightDto> map(final List<Weight> weights);

    void map(final Weight weight, @MappingTarget final Weight existyingEntity);

    @Mapping(source = "weightDto.day", target = "day", qualifiedByName = "dayDefault")
    Weight map(final WeightDto weightDto, final Long userId);

    @Named("dayDefault")
    default Date portionOrNull(final LocalDate localDate) {
        if (localDate == null) return Date.valueOf(LocalDate.now());
        return Date.valueOf(localDate);
    }
}
