package org.example.authservice.component.auth.filter;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.authservice.common.object.FilterResponse;
import org.example.authservice.common.util.TokenUtil;
import org.example.authservice.component.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class TokenFilter extends OncePerRequestFilter {

    private final AuthService authService;

    @Override
    @SneakyThrows
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        if (request.getServletPath().contains("/api/v1/auth/registration") || request.getServletPath().contains("/api/v1/auth/authorization")) {
            filterChain.doFilter(request, response);
        } else {
            String tokenHeader = request.getHeader("Authorization");

            if (Objects.isNull(tokenHeader)) {
                writeResponse(request, response, "Unable to parse Token", HttpStatus.BAD_REQUEST);
                return;
            }

            String token = tokenHeader.substring(7);
            if (TokenUtil.verifyToken(token)) {
                if (request.getServletPath().contains("/api/v1/auth/acceptIp") || request.getServletPath().contains("/api/v1/auth/acceptMail")) {
                    Authentication authentication = new UsernamePasswordAuthenticationToken(authService.getUsernameByToken(token), null, new ArrayList<>());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    filterChain.doFilter(request, response);
                } else if (authService.ipCanUseThisToken(request.getRemoteAddr(), token)) {
                    Authentication authentication = new UsernamePasswordAuthenticationToken(authService.getUsernameByToken(token), null, new ArrayList<>());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    filterChain.doFilter(request, response);
                } else {
                    writeResponse(request, response, "This ip can't use this token", HttpStatus.BAD_REQUEST);
                }
            } else {
                writeResponse(request, response, "Token has not been verified", HttpStatus.BAD_REQUEST);
            }
        }
    }

    @SneakyThrows
    private void writeResponse(HttpServletRequest request, HttpServletResponse response, String message, HttpStatus httpStatus) {
        response.setStatus(httpStatus.value());
        response.getWriter().write(
                FilterResponse.builder()
                        .timestamp(OffsetDateTime.now().toString())
                        .path(request.getServletPath())
                        .method(request.getMethod())
                        .status(httpStatus.value())
                        .error(message)
                        .build()
                        .toString()
        );
        response.getWriter().flush();
        response.getWriter().close();
    }
}
