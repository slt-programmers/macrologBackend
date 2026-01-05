package slt.dto;

import lombok.*;

import java.util.List;

@Builder
@Getter
public class Export {

    private List<FoodDto> allFood;
    private List<EntryDto> allLogEntries;
    private List<SettingDto> allSettingDtos;
    private List<WeightDto> allWeights;
    private List<ActivityDto> allActivities;
    private List<DishDto> allDishes;
    private List<MealplanDto> allMealplans;

}
