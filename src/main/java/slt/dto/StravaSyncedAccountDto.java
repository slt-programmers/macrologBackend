package slt.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StravaSyncedAccountDto {

    private String image;
    private String name;
    private Long syncedAccountId;
    private Long numberActivitiesSynced;
    private Integer syncedApplicationId;

}
