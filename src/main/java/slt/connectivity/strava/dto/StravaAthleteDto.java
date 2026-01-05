package slt.connectivity.strava.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StravaAthleteDto {

    private Long id;
    private String username;
    private String firstname;
    private String lastname;
    private String profile_medium;
    private String profile;

}
