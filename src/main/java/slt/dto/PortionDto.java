package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PortionDto {

    private Long id;
    @ApiModelProperty(notes = "Als je op food niveau grams hebt gekozen dan is dit de hoeveelheid gram", example = "100.0")
    private String description;
    private Double grams;
    @ApiModelProperty(notes = "bord oid", required = true, example = "bord")
    private Macro macros;

    public PortionDto(Long id, String description, Double grams) {
        this.id = id;
        this.description = description;
        this.grams = grams;
    }

}
