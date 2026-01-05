package slt.database.entities;

import lombok.*;

import jakarta.persistence.*;
import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "settings")
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition="bigint")
    private Long id;

    @Column(name = "setting")
    private String name;

    @Column(length = 65535, columnDefinition="TEXT")
    private String value;

    @Column(name = "date")
    private Date day;

    @Column(name = "user_id")
    private Long userId;

}
