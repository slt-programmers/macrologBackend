package csl.dto;
import csl.enums.MeasurementUnit;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class Export {

    public List<Food> allFood;
    public List<LogEntry> allLogEntries;
    public List<Setting> allSettings;

    public List<Food> getAllFood() {
        return allFood;
    }

    public void setAllFood(List<Food> allFood) {
        this.allFood = allFood;
    }

    public List<LogEntry> getAllLogEntries() {
        return allLogEntries;
    }

    public void setAllLogEntries(List<LogEntry> allLogEntries) {
        this.allLogEntries = allLogEntries;
    }

    public List<Setting> getAllSettings() {
        return allSettings;
    }

    public void setAllSettings(List<Setting> allSettings) {
        this.allSettings = allSettings;
    }
}
