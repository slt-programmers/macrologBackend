package slt.database.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {

    private long id;
    private String username;
    private String password;
    private String email;
    private String resetPassword;
    private LocalDateTime resetDate;

}
