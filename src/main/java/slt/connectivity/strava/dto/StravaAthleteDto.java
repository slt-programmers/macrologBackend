package slt.connectivity.strava.dto;

import lombok.Builder;
import lombok.Data;

@Data
@SuppressWarnings("all")
@Builder public class StravaAthleteDto {
    Long id;
    String username;
    String firstname;
    String lastname;
    String profile_medium;
    String profile;
}
