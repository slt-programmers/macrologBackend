package slt.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Entry;
import slt.database.entities.Portion;
import slt.dto.EntryDto;
import slt.dto.MacroDto;
import slt.dto.PortionDto;
import slt.util.MacroUtils;

import java.util.List;

@Mapper(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface EntryMapper {

    EntryMapper INSTANCE = Mappers.getMapper(EntryMapper.class);

    @Mapping(source = "entry", target = "macrosCalculated", qualifiedByName = "macrosCalculated")
    EntryDto map(final Entry entry);

    @Named("macrosCalculated")
    default MacroDto macrosCalculated(final Entry entry) {
        final var macroDto =  MacroUtils.calculateMacro(entry.getFood(), entry.getPortion());
        return MacroUtils.multiply(macroDto, entry.getMultiplier());
    }

    List<EntryDto> map(final List<Entry> entries);

    @Mapping(source = "entryDto.portion", target = "portion", qualifiedByName = "portionNullable")
    Entry map(final EntryDto entryDto, final Long userId);

    @Named("portionNullable")
    default Portion portionNullable(final PortionDto portionDto) {
        if (portionDto == null) return null;
        return Portion.builder().id(portionDto.getId()).build();
    }

}
