package slt.util;

import org.apache.commons.codec.digest.DigestUtils;

public class PasswordUtils {

    private PasswordUtils(){}

    public static String hashPassword(final String password) {
        return DigestUtils.sha256Hex(password); //NOSONAR
    }
}
