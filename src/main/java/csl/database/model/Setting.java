package csl.database.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Setting {

    private Long id;
    private String name;
    private String value;
    private Date day = Date.valueOf(LocalDate.now());

}
