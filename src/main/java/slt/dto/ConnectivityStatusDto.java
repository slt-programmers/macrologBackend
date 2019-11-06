package slt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectivityStatusDto {

    private boolean connected;
    private String syncedApplicationId;
}
