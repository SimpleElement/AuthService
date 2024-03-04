package org.example.authservice.component.auth.service.validator;

import org.example.authservice.common.throwable.exception.BadRequestException;
import org.example.authservice.common.throwable.exception.LargeNumberOfRequestsException;

public class AuthAssert {
    public static void isUsernameExists(Boolean res, String message) {
        if (!res)
            return;
        throw new BadRequestException(message);
    }

    public static void isEmailRegistration(Boolean res, String message) {
        if (!res)
            return;
        throw new BadRequestException(message);
    }

    public static void canWriteMessage(Boolean res, String message) {
        if (res)
            return;
        throw new LargeNumberOfRequestsException(message);
    }
}
