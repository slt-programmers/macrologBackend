package slt.dto;

import slt.database.model.Setting;
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
    private List<LogEntryDto> allLogEntries;
    private List<Setting> allSettings;
    private List<WeightDto> allWeights;
    private List<LogActivityDto> allActivities;

}
