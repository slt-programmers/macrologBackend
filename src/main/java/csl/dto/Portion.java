package csl.dto;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Carmen on 6-7-2018.
 */
public class Portion {

    private Long id;
    @ApiModelProperty(notes = "Als je op food niveau grams hebt gekozen dan is dit de hoeveelheid gram",required=false, example = "100.0")
    private Double grams;
    @ApiModelProperty(notes = "als je op food niveau unitMultiplier hebt gekozen. dan is dit aantal malen dat food",required=false, example = "1.2")
    private Double unitMultiplier;
    @ApiModelProperty(notes = "bord oid",required=true, example = "bord")
    private String description;
    private Macro calculatedMacros;


    public Double getGrams() {
        return grams;
    }

    public void setGrams(Double grams) {
        this.grams = grams;
    }

    public Double getUnitMultiplier() {
        return unitMultiplier;
    }

    public void setUnitMultiplier(Double unitMultiplier) {
        this.unitMultiplier = unitMultiplier;
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

    public Portion(Long id, String description, Double grams, Double unitMultiplier) {
        this.id = id;
        this.description = description;
        this.grams = grams;
        this.unitMultiplier = unitMultiplier;
    }

    public Portion() {
    }

    public Macro getCalculatedMacros() {
        return calculatedMacros;
    }

    public void setCalculatedMacros(Macro calculatedMacros) {
        this.calculatedMacros = calculatedMacros;
    }
}
