package slt.database.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "useraccounts")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;
    @Column(length = 65535, columnDefinition="clob")
    private String password;
    @Column(length = 65535, columnDefinition="clob")
    private String email;
    @Column(length = 65535, columnDefinition="clob")
    private String resetPassword;
    private LocalDateTime resetDate;
    @Column(name = "is_admin")
    private boolean isAdmin;

    @OneToMany (
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "user_id")
    private List<Setting> settings = new ArrayList<>();

}
