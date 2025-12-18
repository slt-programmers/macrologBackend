package slt.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FoodRequest {

    private Long id;
    private String name;
    private Double protein;
    private Double fat;
    private Double carbs;
    private List<PortionDto> portions;

}
