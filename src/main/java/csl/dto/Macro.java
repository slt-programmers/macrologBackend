package csl.dto;

import io.swagger.annotations.ApiModelProperty;

/**
 * Class voor het bewaren van de macros
 */
public class Macro {

    @ApiModelProperty(notes = "Number of proteins",required=true)
    private Integer proteins;
    @ApiModelProperty(notes = "Number of fat",required=true)
    private Integer fat;
    @ApiModelProperty(notes = "Number of carbs",required=true)
    private Integer carbs;

    public Integer getProteins() {
        return proteins;
    }

    public void setProteins(Integer proteins) {
        this.proteins = proteins;
    }

    public Integer getFat() {
        return fat;
    }

    public void setFat(Integer fat) {
        this.fat = fat;
    }

    public Integer getCarbs() {
        return carbs;
    }

    public void setCarbs(Integer carbs) {
        this.carbs = carbs;
    }
}
