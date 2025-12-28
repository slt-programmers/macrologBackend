package slt.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import slt.database.entities.Portion;
import slt.dto.PortionDto;

import java.util.List;

@Mapper(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface PortionMapper {

    PortionDto map(final Portion portion);

    List<PortionDto> map(final List<Portion> portions);
}
