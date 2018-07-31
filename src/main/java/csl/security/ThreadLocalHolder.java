package csl.security;

public class ThreadLocalHolder {
    static private ThreadLocal threadLocal = new ThreadLocal<UserInfo>();

    public static ThreadLocal<UserInfo> getThreadLocal() {
        return threadLocal;
    }
}
