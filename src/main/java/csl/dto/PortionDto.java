package csl.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Carmen on 6-7-2018.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortionDto {

    private Long id;
    @ApiModelProperty(notes = "Als je op food niveau grams hebt gekozen dan is dit de hoeveelheid gram", example = "100.0")
    private Double grams;
    @ApiModelProperty(notes = "bord oid",required=true, example = "bord")
    private String description;
    private Macro macros;

    public PortionDto(Long id, String description, Double grams) {
        this.id = id;
        this.description = description;
        this.grams = grams;
    }

    public PortionDto() {
    }

    public Double getGrams() {
        return grams;
    }

    public void setGrams(Double grams) {
        this.grams = grams;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Macro getMacros() {
        return macros;
    }

    public void setMacros(Macro macros) {
        this.macros = macros;
    }
}
