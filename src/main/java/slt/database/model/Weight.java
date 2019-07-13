package slt.database.model;

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
public class Weight {

    private Long id;
    private Double weight = 0.0;
    private Date day = Date.valueOf(LocalDate.now());
    private String remark;

}
