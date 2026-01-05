package slt.connectivity.strava.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityDetailsDto {

    private Long id;
    private String name;
    private Double calories;
    private String type;
    private String start_date;  // e.g. "2018-02-16T14:52:54Z"

}

