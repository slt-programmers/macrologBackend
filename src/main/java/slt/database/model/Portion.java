package slt.database.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Portion {

    private Long id;
    @ApiModelProperty(notes = "bord oid", required = true, example = "bord")
    private String description;
    @ApiModelProperty(notes = "Als je op food niveau grams hebt gekozen dan is dit de hoeveelheid gram", example = "100.0")
    private Double grams;

}
