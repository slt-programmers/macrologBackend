package slt.connectivity.google.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Oath2Token {

    private Long expires_at;
    private Long expires_in;
    private String refresh_token;
    private String access_token;

}
