package slt.database.entities;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "logentry")
@Builder
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition="bigint")
    private Long id;

    @Column(name = "food_id")
    private Long foodId;

    @Column(name = "portion_id")
    private Long portionId;

    private Double multiplier;

    private Date day;

    private String meal;

    @Column(name = "user_id")
    private Integer userId;
}
