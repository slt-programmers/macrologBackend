package slt.connectivity.strava.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("all")
public class ListedActivityDto {
    long id;
    String name;
    String type;
    DateTime start_date_local;
    Double calories;
}
