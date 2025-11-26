package slt.database.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Mealtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition="bigint")
    private Integer id;

    private String meal;

    private String weekday;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mealplan_id")
    private Mealplan mealplan;

    @OneToMany(mappedBy = "mealtime", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ingredient> ingredients;
}
