package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeightDto {

    private Long id;
    private Double weight;
    @ApiModelProperty(notes = "Day of log", required = true)
    private LocalDate day;
    private String remark;

}
