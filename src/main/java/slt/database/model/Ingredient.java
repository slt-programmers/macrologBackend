package slt.database.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {

    private Long id;
    private Long mealId;
    private Long foodId;
    private Long portionId;
    private Double multiplier;

}
