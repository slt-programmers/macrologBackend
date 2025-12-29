package slt.database.entities;

import lombok.*;

import jakarta.persistence.*;

import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Weight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint")
    private Long id;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "day")
    private Date day;

    @Column(length = 65535, columnDefinition = "TEXT")
    private String remark;

    @Column(name = "user_id")
    private Long userId;

}
