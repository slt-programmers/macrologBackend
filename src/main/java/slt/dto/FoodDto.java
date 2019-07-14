package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FoodDto {

    private Long id;
    private String name;
    private Double protein;
    private Double fat;
    private Double carbs;

    @Singular
    private List<PortionDto> portions = new ArrayList<>();

    public void addPortion(PortionDto currDto) {
        portions.add(currDto);
    }

}
