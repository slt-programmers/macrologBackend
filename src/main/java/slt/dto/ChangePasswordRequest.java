package slt.dto;

import lombok.*;

@Getter
@Builder
public class ChangePasswordRequest {

    private String oldPassword;
    private String newPassword;
    private String confirmPassword;

}
