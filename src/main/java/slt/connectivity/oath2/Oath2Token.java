package slt.connectivity.oath2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("all")
public class Oath2Token {

    Long expires_at;
    Long expires_in;
    String refresh_token;
    String access_token;

}
