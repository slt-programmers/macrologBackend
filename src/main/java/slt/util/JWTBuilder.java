package slt.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static slt.security.SecurityConstants.EXPIRATION_TIME;
import static slt.security.SecurityConstants.SECRET;

public class JWTBuilder {

    public String generateJWT(String name,  Integer userId) {
        return generateJWT(name, userId, new Date(System.currentTimeMillis() + EXPIRATION_TIME));
    }

    public String generateJWT(String name, Integer userId, Date expirationDate) {
        return Jwts.builder()
                .setSubject("users/TzMUocMF4p")
                .setExpiration(expirationDate)
                .claim("name", name)
                .claim("userId", userId)
                .signWith(
                        SignatureAlgorithm.HS256,
                        SECRET.getBytes(StandardCharsets.UTF_8)
                )
                .compact();
    }
}
