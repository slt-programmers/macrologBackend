package slt.database.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition="bigint")
    private Long id;

    private String name;
    private Double protein;
    private Double fat;
    private Double carbs;

    @Column(name = "user_id")
    private Integer userId;

}
