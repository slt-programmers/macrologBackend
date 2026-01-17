package slt.dto;

import lombok.*;

@Getter
@Builder
public class AuthenticationRequest {

    private String username; // Or email
    @Deprecated
    private String email;
    private String password;

}