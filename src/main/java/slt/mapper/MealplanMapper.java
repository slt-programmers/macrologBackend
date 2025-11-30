package slt.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import slt.database.entities.Mealplan;
import slt.database.entities.Mealtime;
import slt.dto.MealplanDto;
import slt.dto.requests.MealplanRequest;

@Mapper(uses = MealtimeMapper.class)
public interface MealplanMapper {

    MealplanMapper INSTANCE = Mappers.getMapper(MealplanMapper.class);

    MealplanDto map(final Mealplan mealplan);

    @Mapping(source = "request.mealtimes", target = "mealtimes")
    Mealplan map(final MealplanRequest request, final Integer userId);

    @AfterMapping
    default void setParentInChildren(final MealplanRequest request, @MappingTarget Mealplan mealplan) {
        if (mealplan.getMealtimes() != null) {
            for (Mealtime mealtime : mealplan.getMealtimes()) {
                mealtime.setMealplan(mealplan);
            }
        }
    }

}
