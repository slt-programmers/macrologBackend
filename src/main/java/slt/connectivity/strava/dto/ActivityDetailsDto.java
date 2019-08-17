package slt.connectivity.strava.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("all")
public class ActivityDetailsDto {
    long id;
    String name;
    Double calories;
    String type;
    String start_date;  // e.g. "2018-02-16T14:52:54Z"
}

