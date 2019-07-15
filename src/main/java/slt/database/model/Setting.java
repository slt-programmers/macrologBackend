package slt.database.model;

import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.sql.Date;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setting {

    private Long id;
    private String name;
    private String value;
    private Date day = Date.valueOf(LocalDate.now());


}
