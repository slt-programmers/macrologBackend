package slt.connectivity;

import lombok.Data;

@Data
@SuppressWarnings("all")
public class StravaAthlete {
    Long id;
    String username;
    String firstname;
    String lastname;
    String profile_medium;
}
