package slt.security;

public class ThreadLocalHolder {
    private static ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    private ThreadLocalHolder() {
    }

    public static ThreadLocal<UserInfo> getThreadLocal() {
        return threadLocal;
    }
}
