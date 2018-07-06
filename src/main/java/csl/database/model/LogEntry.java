package csl.database.model;

import io.swagger.annotations.ApiModelProperty;
import org.joda.time.DateTime;

import java.sql.Date;

/**
 * Created by Carmen on 18-3-2018.
 */
public class LogEntry {

    private Long id;
    private Long foodId;
    private Long aliasIdUsed;
    private Double multiplier;
    private Date day;
    private String meal;

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public String getMeal() {
        return meal;
    }

    public void setMeal(String meal) {
        this.meal = meal;
    }

    public LogEntry() {
    }

    public LogEntry(Long id, Long foodId, Long aliasIdUsed, Double multiplier, Date day,String meal) {
        this.id = id;
        this.foodId = foodId;
        this.aliasIdUsed = aliasIdUsed;
        this.multiplier = multiplier;
        this.day = day;
        this.meal=meal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public Long getAliasIdUsed() {
        return aliasIdUsed;
    }

    public void setAliasIdUsed(Long aliasIdUsed) {
        this.aliasIdUsed = aliasIdUsed;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }
}
