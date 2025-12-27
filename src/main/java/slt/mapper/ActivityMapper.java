package slt.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Activity;
import slt.dto.ActivityDto;

import java.util.List;

@Mapper(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface ActivityMapper {

    ActivityMapper INSTANCE = Mappers.getMapper(ActivityMapper.class);

    ActivityDto map(final Activity activity);

    List<ActivityDto> map(final List<Activity> activities);

    Activity map(final ActivityDto activityDto, final Long userId);
}
