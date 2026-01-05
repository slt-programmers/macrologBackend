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
public class StravaToken {

    private Long expires_at;
    private Long expires_in;
    private String refresh_token;
    private String access_token;
    private StravaAthleteDto athlete;

}
