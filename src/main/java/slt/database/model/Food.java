package slt.database.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Food {

    private Long id;
    private String name;
    private Double protein;
    private Double fat;
    private Double carbs;

}
