package slt.database.entities;

import lombok.*;

import jakarta.persistence.*;
import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "logentry")
@Builder
public class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition="bigint")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "food_id")
    private Food food;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "portion_id")
    private Portion portion;

    private Double multiplier;

    private Date day;

    private String meal;

    @Column(name = "user_id")
    private Long userId;
}
