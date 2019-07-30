package slt.database.entities;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Portion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition="bigint")
    private Long id;

    @ApiModelProperty(notes = "bord oid", required = true, example = "bord")
    private String description;

    @ApiModelProperty(notes = "Als je op food niveau grams hebt gekozen dan is dit de hoeveelheid gram", example = "100.0")
    private Double grams;

    @Column(name = "food_id")
    private Integer foodId;


}
