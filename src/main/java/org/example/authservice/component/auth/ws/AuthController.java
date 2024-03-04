package org.example.authservice.component.auth.ws;

import lombok.RequiredArgsConstructor;
import org.example.authservice.component.auth.service.AuthService;
import org.example.authservice.component.auth.ws.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth/")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registration")
    public TokenDto registration(@Valid @RequestBody UserRegistrationDto userRegistrationDto, HttpServletRequest request) {
        return authService.registration(userRegistrationDto, request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    @PostMapping("/acceptMail")
    public ResponseEntity<Object> removeToken(@Valid @RequestBody CodeDto code, HttpServletRequest request) {
        authService.acceptMail(code, request.getRemoteAddr(), request.getHeader("User-Agent"));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/repeatEmailCode")
    public ResponseEntity<Object> repeatEmailCode(HttpServletRequest request) {
        authService.repeatEmailCode(request.getRemoteAddr(), request.getHeader("User-Agent"));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/authorization")
    public TokenDto authorization(@Valid @RequestBody UserAuthorizationDto userAuthorizationDto, HttpServletRequest request) {
        return authService.authorization(userAuthorizationDto, request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    @PostMapping("/acceptAuthorization")
    public ResponseEntity<Object> acceptAuthorization(@Valid @RequestBody CodeDto code, HttpServletRequest request) {
        authService.acceptAuthorization(code, request.getRemoteAddr(), request.getHeader("User-Agent"));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/repeatAuthorizationCode")
    public ResponseEntity<Object> repeatAuthorizationCode(HttpServletRequest request) {
        authService.repeatAuthorizationCode(request.getRemoteAddr(), request.getHeader("User-Agent"));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/addCurrentIp")
    public ResponseEntity<Object> addCurrentIp(HttpServletRequest request) {
        authService.addCurrentIp(request.getRemoteAddr(), request.getHeader("User-Agent"));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/acceptCurrentIp")
    public ResponseEntity<Object> acceptCurrentIp(@Valid @RequestBody CodeDto code, HttpServletRequest request) {
        authService.acceptCurrentIp(code, request.getRemoteAddr(), request.getHeader("User-Agent"));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/repeatIpAddressCode")
    public ResponseEntity<Object> repeatIpAddressCode(HttpServletRequest request) {
        authService.repeatIpAddressCode(request.getRemoteAddr(), request.getHeader("User-Agent"));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/removeIpAddress")
    public ResponseEntity<Object> removeIpAddress(@Valid @RequestBody RemoveIpDto removeIpDto, HttpServletRequest request) {
        authService.removeIpAddress(removeIpDto, request.getRemoteAddr(), request.getHeader("User-Agent"));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/removeToken")
    public ResponseEntity<Object> removeToken(@Valid @RequestBody TokenDto token, HttpServletRequest request) {
        authService.removeToken(token, request.getRemoteAddr(), request.getHeader("User-Agent"));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
