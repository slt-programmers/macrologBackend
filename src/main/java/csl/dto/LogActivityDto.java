package csl.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogActivityDto {

    private Long id;
    @ApiModelProperty(notes = "Name", required = true, example = "Cycling")
    private String name;
    @ApiModelProperty(notes = "Amount of calories burned", required = true, example = "1.7")
    private Double calories;
    @ApiModelProperty(notes = "Day of activity", required = true)
    private Date day;

    public LogActivityDto() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getCalories() {
        return calories;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }
}
