package csl.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddFoodRequest {

    @ApiModelProperty(notes = "ID van food. Indien gevuld is het een update")
    private Long id;
    private String name;
    private Double protein;
    private Double fat;
    private Double carbs;
    private List<PortionDto> portions;

}
