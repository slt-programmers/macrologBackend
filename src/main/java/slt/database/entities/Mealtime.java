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
@Table(name = "mealtime")
public class Mealtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint")
    private Long id;

    private String meal;

    private String weekday;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mealplan_id")
    private Mealplan mealplan;

    @OneToMany(mappedBy = "mealtime", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Ingredient> ingredients;

    public void addIngredient(final Ingredient ingredient) {
        ingredients.add((ingredient));
        ingredient.setMealtime(this);
    }

    public void removeIngredient(final Ingredient ingredient) {
        ingredients.remove(ingredient);
        ingredient.setMealtime(null);
    }
}
