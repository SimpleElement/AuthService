package org.example.authservice.common.util;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResponseUtil {
    public static Map<String, Object> getErrorBody(HttpStatus status, WebRequest webRequest, String message) {
        Map<String, Object> body = getErrorBody(status, webRequest);
        body.put("error", message);
        return body;
    }

    public static List<String> constraintErrors(BindingResult bindingResult) {
        return bindingResult
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
    }

    private static Map<String, Object> getErrorBody(HttpStatus status, WebRequest webRequest) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now());

        if (webRequest instanceof ServletWebRequest) {
            HttpServletRequest request = ((ServletWebRequest) webRequest).getRequest();
            body.put("path", request.getRequestURI());
            body.put("method", request.getMethod());
        }
        body.put("status", status.value());
        return body;
    }
}
