package slt.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;

import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Mealplan;
import slt.database.entities.Mealtime;
import slt.dto.MealplanDto;

import java.util.List;

@Mapper(uses = MealtimeMapper.class,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface MealplanMapper {

    MealplanMapper INSTANCE = Mappers.getMapper(MealplanMapper.class);

    MealplanDto map(final Mealplan mealplan);

    List<MealplanDto> map(final List<Mealplan> mealplans);

    Mealplan map(final MealplanDto dto, final Long userId);

    @AfterMapping
    default void setParentInChildren(@MappingTarget Mealplan mealplan) {
        if (mealplan.getMealtimes() != null) {
            for (Mealtime mealtime : mealplan.getMealtimes()) {
                mealtime.setMealplan(mealplan);
            }
        }
    }

}
