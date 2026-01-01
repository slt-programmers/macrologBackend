package slt.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettingDto {

    private Integer id;
    private String name;
    private String value;
    @Builder.Default
    private LocalDate day = LocalDate.now();

}
