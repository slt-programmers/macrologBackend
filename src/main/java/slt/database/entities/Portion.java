package slt.database.entities;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    private Food food;
}
