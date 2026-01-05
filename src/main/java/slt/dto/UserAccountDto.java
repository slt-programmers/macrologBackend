package slt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAccountDto {

    private Long id;
    private String token;
    private String userName;
    private String email;
    private boolean isAdmin;

}
