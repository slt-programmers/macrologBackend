package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeightDto {

    private Long id;
    private Double weight;
    private LocalDate day;
    private String remark;

}
