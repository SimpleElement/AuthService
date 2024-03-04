package org.example.authservice.common.util;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.SneakyThrows;
import org.example.authservice.component.auth.model.TokenEntity;
import org.example.authservice.component.auth.model.UserEntity;

import java.time.LocalDateTime;

public class TokenUtil {

    private static String secret = "kvKHL5jkO4wrWWzyH+2fEhOnBp/a9nAwDD2XTE8GFmE=";

    @SneakyThrows
    public static TokenEntity generateToken(UserEntity user, String userAgent) {
        JWSSigner signer = new MACSigner(secret);

        JWTClaimsSet token = new JWTClaimsSet.Builder()
                .claim("username", user.getUsername())
                .claim("pop", String.valueOf((int) (Math.random() * 10000)))
                .build();
        SignedJWT accessTokenJWS = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), token);
        accessTokenJWS.sign(signer);

        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setUser(user);
        tokenEntity.setToken(accessTokenJWS.serialize());
        tokenEntity.setLastActive(LocalDateTime.now());
        tokenEntity.setUserAgent(userAgent);

        return tokenEntity;
    }

    @SneakyThrows
    public static boolean verifyToken(String token) {
        try {
            return SignedJWT.parse(token).verify(new MACVerifier(secret));
        } catch (Exception e) {
            return false;
        }
    }
}
