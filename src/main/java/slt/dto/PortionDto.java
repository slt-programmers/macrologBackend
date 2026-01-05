package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
public class PortionDto {

    @Setter
    private Long id;
    private String description;
    private Double grams;
    @Setter
    private MacroDto macros;

}
