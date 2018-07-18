package csl.dto;
import csl.database.model.Setting;

import java.util.List;

public class Export {

    private List<FoodDto> allFoodDto;
    private List<LogEntryDto> allLogEntries;
    private List<Setting> allSettings;

    public List<FoodDto> getAllFoodDto() {
        return allFoodDto;
    }

    public void setAllFoodDto(List<FoodDto> allFoodDto) {
        this.allFoodDto = allFoodDto;
    }

    public List<LogEntryDto> getAllLogEntries() {
        return allLogEntries;
    }

    public void setAllLogEntries(List<LogEntryDto> allLogEntries) {
        this.allLogEntries = allLogEntries;
    }

    public List<Setting> getAllSettings() {
        return allSettings;
    }

    public void setAllSettings(List<Setting> allSettings) {
        this.allSettings = allSettings;
    }

}
