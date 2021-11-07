package slt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogEntryRequest {

    private Long id;
    private Long foodId;
    private Long portionId;
    private Double multiplier;
    private Date day;
    private String meal;
}
