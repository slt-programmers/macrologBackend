package csl.database.model;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Carmen on 6-7-2018.
 */
public class Portion {

    private Long id;
    @ApiModelProperty(notes = "Als je op food niveau grams hebt gekozen dan is dit de hoeveelheid gram", example = "100.0")
    private Double grams;
    @ApiModelProperty(notes = "bord oid", required = true, example = "bord")
    private String description;

    public Portion(Long id, String description, Double grams) {
        this.id = id;
        this.description = description;
        this.grams = grams;
    }

    public Portion() {
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
}
