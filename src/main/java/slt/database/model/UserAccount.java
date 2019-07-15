package slt.database.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccount {

    private long id;
    private String username;
    private String password;
    private String email;
    private String resetPassword;
    private LocalDateTime resetDate;

}
