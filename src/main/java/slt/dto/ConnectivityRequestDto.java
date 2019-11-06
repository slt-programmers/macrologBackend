package slt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectivityRequestDto {

    private String clientAuthorizationCode;
}
