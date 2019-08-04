package slt.connectivity.strava.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("all")
public class StravaAthleteDto {
    Long id;
    String username;
    String firstname;
    String lastname;
    String profile_medium;
    String profile;
}
