package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PortionDto {

    private Long id;
    private String description;
    private Double grams;
    private MacroDto macros;

}
