package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSettingsDto {

    private String name;
    private String gender;
    private Integer age;
    private LocalDate birthday;
    private Integer height;
    private Double currentWeight;
    private Double activity;

    private Integer goalProtein;
    private Integer goalFat;
    private Integer goalCarbs;

}
