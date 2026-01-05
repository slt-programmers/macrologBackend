package slt.connectivity.strava.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListedActivityDto {

    private Long id;
    private String name;
    private String type;

}
