package slt.dto;

import lombok.*;

@Getter
@Builder
public class ResetPasswordRequest {

    private String email;

}