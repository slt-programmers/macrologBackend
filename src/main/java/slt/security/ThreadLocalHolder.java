package slt.security;

import lombok.Getter;

public class ThreadLocalHolder {
    @Getter
    private static ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    private ThreadLocalHolder() {
    }

}
