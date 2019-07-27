package slt.database.entities;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "activity")
@Builder
public class LogActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition="bigint")
    private Long id;

    private String name;
    private Double calories;
    private Date day;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "synced_with")
    private String syncedWith;

    @Column(name = "synced_id")
    private Long syncedId;

}
