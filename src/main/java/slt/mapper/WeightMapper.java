package slt.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Weight;
import slt.dto.WeightDto;

import java.util.List;

@Mapper(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface WeightMapper {

    WeightMapper INSTANCE = Mappers.getMapper(WeightMapper.class);

    WeightDto map(final Weight weight);

    List<WeightDto> map(final List<Weight> weights);

    void map(final Weight weight, @MappingTarget final Weight existyingEntity);

    Weight map(final WeightDto weightDto, final Long userId);
}
