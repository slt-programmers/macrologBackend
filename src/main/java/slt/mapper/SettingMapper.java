package slt.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Setting;
import slt.dto.SettingDto;

@Mapper
public interface SettingMapper {

    SettingMapper INSTANCE = Mappers.getMapper(SettingMapper.class);

    Setting map(final SettingDto settingDto, final Long userId);
}
