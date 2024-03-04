package org.example.authservice.common.util;

import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class Sha256PasswordUtil {

    @SneakyThrows
    public static String hashCode(String password) {
        return Base64.getEncoder()
                .encodeToString(MessageDigest.getInstance("SHA-256")
                        .digest(password.getBytes(StandardCharsets.UTF_8))
                );
    }

}
