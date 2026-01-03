package slt.mapper;

import org.mapstruct.Mapper;
import slt.database.entities.Portion;
import slt.dto.PortionDto;

import java.util.List;

@Mapper
public interface PortionMapper {

    PortionDto map(final Portion portion);

    List<PortionDto> map(final List<Portion> portions);

    Portion map(final PortionDto portionDto);
}
