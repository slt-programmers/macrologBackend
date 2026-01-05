package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Date;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityDto {

    private Long id;

    private String name;

    private Double calories;

    private Date day;

    private String syncedWith;

    private Long syncedId;

}
