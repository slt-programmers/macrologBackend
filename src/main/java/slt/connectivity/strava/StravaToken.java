package slt.connectivity.strava;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import slt.connectivity.strava.dto.StravaAthleteDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("all")
public class StravaToken {

    Long expires_at;
    Long expires_in;
    String refresh_token;
    String access_token;
    StravaAthleteDto athlete;

}
