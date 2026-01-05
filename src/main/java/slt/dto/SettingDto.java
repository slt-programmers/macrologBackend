package slt.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
public class SettingDto {

    private Integer id;
    private String name;
    private String value;
    @Setter
    @Builder.Default
    private LocalDate day = LocalDate.now();

}
