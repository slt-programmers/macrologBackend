package slt.dto;

import lombok.*;

@Getter
@Builder
public class ConnectivityStatusDto {

    private boolean connected;
    private String syncedApplicationId;

}
