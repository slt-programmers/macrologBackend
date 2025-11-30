package slt.database.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "mealplan")
public class Mealplan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint")
    private Long id;

    private String title;

    @Column(name = "user_id")
    private Integer userId;

    @OneToMany(mappedBy = "mealplan", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Mealtime> mealtimes = new ArrayList<>();

    public void addMealtime(final Mealtime mealtime) {
        mealtimes.add(mealtime);
        mealtime.setMealplan(this);
    }

    public void removeMealtime(final Mealtime mealtime) {
        mealtimes.remove(mealtime);
        mealtime.setMealplan(null);
    }
}