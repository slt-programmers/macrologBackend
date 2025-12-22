package slt.security;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserInfo {

    private Long userId;

    @Override
    public String toString() {
        return "userId = " + userId;
    }
}