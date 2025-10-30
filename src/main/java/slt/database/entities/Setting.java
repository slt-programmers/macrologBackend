package slt.database.entities;

import lombok.*;

import jakarta.persistence.*;
import java.sql.Date;
import java.time.LocalDate;

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
    private Integer id;

    @Column(name = "setting")
    private String name;

    @Column(length = 65535, columnDefinition="TEXT")
    private String value;

    @Column(name = "date")
    private Date day = Date.valueOf(LocalDate.now());

    @Column(name = "user_id")
    private Integer userId;

}
