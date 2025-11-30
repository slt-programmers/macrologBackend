package slt.database.entities;

//import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import jakarta.persistence.*;

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

    private String description;

    private Double grams;

    @Column(name = "food_id")
    private Integer foodId;

}
