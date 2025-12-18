package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    private String name;

    private Double calories;

    private Date day;

    private String syncedWith;

    private Long syncedId;
}
