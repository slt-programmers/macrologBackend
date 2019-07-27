package slt.connectivity;

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
public class ActivityDetailsDto {
    long id;
    String name;
    Double calories;
    String type;
    DateTime start_date_local;
}
