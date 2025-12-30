package slt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Export {

    private List<FoodDto> allFood;
    private List<EntryDto> allLogEntries;
    private List<SettingDto> allSettingDtos;
    private List<WeightDto> allWeights;
    private List<ActivityDto> allActivities;
    private List<DishDto> allDishes;
    private List<MealplanDto> allMealplans;

}
