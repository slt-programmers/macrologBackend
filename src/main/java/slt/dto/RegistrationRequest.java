package slt.dto;

import lombok.*;

@Getter
@Builder
public class RegistrationRequest {

    private String username;
    private String password;
    private String email;

}
