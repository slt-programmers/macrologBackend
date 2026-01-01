package slt.security;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    private Long userId;

    @Override
    public String toString() {
        return "UserId = " + userId;
    }
}