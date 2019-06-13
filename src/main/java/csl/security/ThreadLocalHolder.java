package csl.security;

public class ThreadLocalHolder {
    static private ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    public static ThreadLocal<UserInfo> getThreadLocal() {
        return threadLocal;
    }
}
