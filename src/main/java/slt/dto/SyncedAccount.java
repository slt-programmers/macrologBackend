package slt.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SyncedAccount {

    String image;
    String name;
    Long syncedAccountId;
}
