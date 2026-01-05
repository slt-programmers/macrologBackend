package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSettingsDto {

    private String name;
    private String gender;
    private Integer age;
    private LocalDate birthday;
    private Integer height;
    @Setter
    private Double currentWeight;
    private Double activity;

    private Integer goalProtein;
    private Integer goalFat;
    private Integer goalCarbs;

}
