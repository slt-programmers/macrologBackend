package csl.dto;
import csl.database.model.Setting;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Export {

    private List<FoodDto> allFood;
    private List<LogEntryDto> allLogEntries;
    private List<Setting> allSettings;
    private List<WeightDto> allWeights;
    private List<LogActivityDto> allActivities;

}
