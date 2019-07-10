package csl.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddFoodRequest {

    @ApiModelProperty(notes = "ID van food. Indien gevuld is het een update")
    private Long id;
    private String name;
    private Double protein;
    private Double fat;
    private Double carbs;
    private List<PortionDto> portions;

}
