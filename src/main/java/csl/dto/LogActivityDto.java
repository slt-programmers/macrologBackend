package csl.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class LogActivityDto {

    private Long id;
    @ApiModelProperty(notes = "Name", required = true, example = "Cycling")
    private String name;
    @ApiModelProperty(notes = "Amount of calories burned", required = true, example = "1.7")
    private Double calories;
    @ApiModelProperty(notes = "Day of activity", required = true)
    private Date day;
}
