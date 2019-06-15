package csl.dto;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class AddFoodRequest {

    @ApiModelProperty(notes = "ID van food. Indien gevuld is het een update")
    private Long id;
    private String name;
    private Double protein;
    private Double fat;
    private Double carbs;
    private List<PortionDto> portions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<PortionDto> getPortions() {
        return portions;
    }

    public void setPortions(List<PortionDto> portions) {
        this.portions = portions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
