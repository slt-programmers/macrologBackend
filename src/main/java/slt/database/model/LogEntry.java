package slt.database.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {

    private Long id;
    private Long foodId;
    private Long portionId;
    private Double multiplier;
    private Date day;
    private String meal;

}
