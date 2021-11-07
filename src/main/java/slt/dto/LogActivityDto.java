package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogActivityDto {

    private Long id;

    @ApiModelProperty(notes = "Name", required = true, example = "Cycling")
    private String name;

    @ApiModelProperty(notes = "Amount of calories burned", required = true, example = "1.7")
    private Double calories;

    @ApiModelProperty(notes = "Day of activity", required = true)
    private Date day;

    private String syncedWith;

    private Long syncedId;
}
