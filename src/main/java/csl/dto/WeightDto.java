package csl.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeightDto {

    private Long id;
    private Double weight;
    @ApiModelProperty(notes = "Day of log", required = true)
    private Date day;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }
}
