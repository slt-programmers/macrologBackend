package csl.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangePasswordRequest {

    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
