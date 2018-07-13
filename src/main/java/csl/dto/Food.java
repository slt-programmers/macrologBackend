package csl.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import csl.enums.MeasurementUnit;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Food {

    private Long id;
    private String name;
    @ApiModelProperty(notes = "Unit van foodrequest. Of GRAMS of UNIT",required=true, example = "GRAMS")
    private MeasurementUnit measurementUnit;
    @ApiModelProperty(notes = "Indien Unit geselecteerd kan hier bijvoorbeeld stuks of bord.",required=false, example = "bord")
    private String unitName;
    @ApiModelProperty(notes = "Optioneel als unit ook nog gewogen wordt. Indien Gram is dit altijd 100",required=true, example = "100")
    private Double unitGrams;
    private Double protein;
    private Double fat;
    private Double carbs;
    private List<Portion> portions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MeasurementUnit getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(MeasurementUnit unit) {
        this.measurementUnit = unit;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public Double getUnitGrams() {
        return unitGrams;
    }

    public void setUnitGrams(Double unitGrams) {
        this.unitGrams = unitGrams;
    }

    public Double getProtein() {
        return protein;
    }

    public void setProtein(Double protein) {
        this.protein = protein;
    }

    public Double getFat() {
        return fat;
    }

    public void setFat(Double fat) {
        this.fat = fat;
    }

    public Double getCarbs() {
        return carbs;
    }

    public void setCarbs(Double carbs) {
        this.carbs = carbs;
    }

    public List<Portion> getPortions() {
        return portions;
    }

    public void setPortions(List<Portion> portions) {
        this.portions = portions;
    }

    public void addPortion(Portion currDto) {
        if (portions == null){
            portions = new ArrayList<>();
        }
        portions.add(currDto);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
