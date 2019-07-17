package slt.database.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition="bigint")
    private Long id;

    @Column(name = "meal_id")
    private Long mealId;

    @Column(name = "food_id")
    private Long foodId;

    @Column(name = "portion_id")
    private Long portionId;

    private Double multiplier;

}
