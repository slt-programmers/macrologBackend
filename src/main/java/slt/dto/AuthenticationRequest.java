package slt.dto;

import lombok.*;

@Getter
@Builder
public class AuthenticationRequest {

    private String username; // Or email
    private String password;

}