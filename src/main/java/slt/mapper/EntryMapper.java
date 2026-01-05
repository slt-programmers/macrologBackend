package slt.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Entry;
import slt.dto.EntryDto;
import slt.dto.MacroDto;
import slt.util.MacroUtils;

import java.util.List;

@Mapper(uses = {FoodMapper.class, PortionMapper.class},
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface EntryMapper {

    EntryMapper INSTANCE = Mappers.getMapper(EntryMapper.class);

    @Mapping(source = "entry", target = "macrosCalculated", qualifiedByName = "macrosCalculated")
    EntryDto map(final Entry entry);

    @Named("macrosCalculated")
    default MacroDto macrosCalculated(final Entry entry) {
        return MacroUtils.calculateMacro(entry.getFood(), entry.getPortion(), entry.getMultiplier());
    }

    List<EntryDto> map(final List<Entry> entries);

    Entry map(final EntryDto entryDto, final Long userId);

}
