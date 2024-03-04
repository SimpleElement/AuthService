package org.example.authservice.component.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.authservice.common.throwable.exception.BadRequestException;
import org.example.authservice.common.util.NumberUtil;
import org.example.authservice.common.util.Sha256PasswordUtil;
import org.example.authservice.common.util.TokenUtil;
import org.example.authservice.component.alerts.AlertsService;
import org.example.authservice.component.auth.model.*;
import org.example.authservice.component.auth.service.repository.*;
import org.example.authservice.component.auth.service.validator.AuthAssert;
import org.example.authservice.component.auth.ws.dto.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AlertsService alertsService;

    private final UserRepository userRepository;
    private final EmailRepository emailRepository;
    private final TokenRepository tokenRepository;
    private final IpAddressRepository ipAddressRepository;
    private final ConfirmEmailRepository confirmEmailRepository;
    private final ConfirmIpAddressRepository confirmIpAddressRepository;
    private final IpAddressEmailMessagesRepository ipAddressEmailMessagesRepository;

    public TokenDto registration(UserRegistrationDto user, String ipAddress, String userAgent) {
        AuthAssert.isUsernameExists(userRepository.existsByUsername(user.getUsername()), "Пользователь с таким именем уже зарегистрирован, попробуйте друге");

        AuthAssert.isEmailRegistration(emailRepository.existsByEmail(user.getEmail()), "Данный email уже зарегистрирован, попробуйте другой или обратитесь в поддержку");

        IpAddressEmailMessagesEntity ipAddressInfo = ipAddressEmailMessagesRepository.getByIpAddress(ipAddress);

        if (Objects.isNull(ipAddressInfo)) {
            ipAddressInfo = new IpAddressEmailMessagesEntity();

            ipAddressInfo.setIpAddress(ipAddress);
            ipAddressInfo.setCountWriteMessage(0);
            ipAddressInfo.setFirstWriteMessage(LocalDateTime.now());
        }

        LocalDateTime timeInIp = ipAddressInfo.getFirstWriteMessage().plusHours(1);
        if (timeInIp.isBefore(LocalDateTime.now())) {
            ipAddressInfo.setCountWriteMessage(0);
            ipAddressInfo.setFirstWriteMessage(LocalDateTime.now());
        }

        ipAddressInfo.setCountWriteMessage(ipAddressInfo.getCountWriteMessage() + 1);

        ipAddressEmailMessagesRepository.save(ipAddressInfo);

        AuthAssert.canWriteMessage(ipAddressInfo.getCountWriteMessage() < 3, "Вы пытались отправить слишком много сообщений на почтовые ящики, попробуйте повторить попытку позже");

        UserEntity userFromDB = new UserEntity();
        userFromDB.setUsername(user.getUsername());
        userFromDB.setPassword(Sha256PasswordUtil.hashCode(user.getPassword()));
        userFromDB = userRepository.save(userFromDB);

        EmailEntity emailFromDB = new EmailEntity();
        emailFromDB.setUser(userFromDB);
        emailFromDB.setEmail(user.getEmail());
        emailFromDB.setIsConfirmed(false);
        emailFromDB = emailRepository.save(emailFromDB);

        ConfirmEmailEntity confirmEmailFromDB = new ConfirmEmailEntity();
        confirmEmailFromDB.setEmail(emailFromDB);
        confirmEmailFromDB.setNumberOfConfirmationAttempts(0);
        confirmEmailFromDB.setRegistrationTime(LocalDateTime.now());
        confirmEmailFromDB.setConfirmCode(Arrays.stream(NumberUtil.getArrRandomInt(8)).mapToObj(String::valueOf).collect(Collectors.joining("")));
        confirmEmailRepository.save(confirmEmailFromDB);

        alertsService.writeRegistrationCode(userFromDB.getUsername(), emailFromDB.getEmail(), confirmEmailFromDB.getConfirmCode());

        TokenEntity tokenFromDB = TokenUtil.generateToken(userFromDB, userAgent);
        tokenFromDB = tokenRepository.save(tokenFromDB);

        IpAddressEntity ipAddressFromDB = new IpAddressEntity();
        ipAddressFromDB.setToken(tokenFromDB);
        ipAddressFromDB.setIpAddress(ipAddress);
        ipAddressFromDB.setIsConfirmed(false);
        ipAddressRepository.save(ipAddressFromDB);

        TokenDto tokenFromUser = new TokenDto();
        tokenFromUser.setToken(tokenFromDB.getToken());
        return tokenFromUser;
    }

    public void acceptMail(CodeDto code, String ipAddress, String userAgent) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        ConfirmEmailEntity confirmEmailEntity = confirmEmailRepository.findByEmailUserUsername(username);

        if (confirmEmailEntity.getConfirmCode().equals(code.getCode())) {
            IpAddressEntity ipAddressEntity = ipAddressRepository.findByIpAddressAndTokenUserUsername(ipAddress, username);

            if (Objects.isNull(ipAddressEntity)) {
                throw new BadRequestException("Ошибка подтверждения IP адреса");
            }

            ipAddressEntity.setIsConfirmed(true);
            ipAddressRepository.save(ipAddressEntity);

            confirmEmailRepository.delete(confirmEmailEntity);

            EmailEntity email = emailRepository.findByUserUsername(username);
            email.setIsConfirmed(true);
            emailRepository.save(email);
            return;
        }

        confirmEmailEntity.setNumberOfConfirmationAttempts(confirmEmailEntity.getNumberOfConfirmationAttempts() + 1);
        confirmEmailRepository.save(confirmEmailEntity);

        if (confirmEmailEntity.getNumberOfConfirmationAttempts() > 4) {
            confirmEmailRepository.delete(confirmEmailRepository.findByEmailUserUsername(username));
            emailRepository.delete(emailRepository.findByUserUsername(username));

            ipAddressRepository.removeAllByTokenUserUsername(username);
            tokenRepository.delete(tokenRepository.getByUserUsername(username));
            userRepository.delete(userRepository.findByUsername(username));

            throw new BadRequestException("Вы потратили все попытки, попробуйте зарегистирироваться ещё раз");
        }

        throw new BadRequestException("Код введён неверно, попробуйте ещё раз");
    }

    public void repeatEmailCode(String ipAddress, String userAgent) {
        IpAddressEmailMessagesEntity ipAddressInfo = ipAddressEmailMessagesRepository.getByIpAddress(ipAddress);

        if (Objects.isNull(ipAddressInfo)) {
            ipAddressInfo = new IpAddressEmailMessagesEntity();

            ipAddressInfo.setIpAddress(ipAddress);
            ipAddressInfo.setCountWriteMessage(0);
            ipAddressInfo.setFirstWriteMessage(LocalDateTime.now());
        }

        LocalDateTime timeInIp = ipAddressInfo.getFirstWriteMessage().plusHours(1);
        if (timeInIp.isBefore(LocalDateTime.now())) {
            ipAddressInfo.setCountWriteMessage(0);
            ipAddressInfo.setFirstWriteMessage(LocalDateTime.now());
        }

        ipAddressInfo.setCountWriteMessage(ipAddressInfo.getCountWriteMessage() + 1);

        ipAddressEmailMessagesRepository.save(ipAddressInfo);

        AuthAssert.canWriteMessage(ipAddressInfo.getCountWriteMessage() < 3, "Вы пытались отправить слишком много сообщений на почтовые ящики, попробуйте повторить попытку позже");

        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserEntity user = userRepository.findByUsername(username);

        ConfirmEmailEntity confirmEmail = confirmEmailRepository.findByEmailUserUsername(username);
        if (Objects.nonNull(confirmEmail)) {
            alertsService.writeRegistrationCode(user.getUsername(), confirmEmail.getEmail().getEmail(), confirmEmail.getConfirmCode());
        } else {
            throw new BadRequestException("Вам не нужно подтверждать почту");
        }
    }

    public TokenDto authorization(UserAuthorizationDto userAuthorizationDto, String ipAddress, String userAgent) {
        IpAddressEmailMessagesEntity ipAddressInfo = ipAddressEmailMessagesRepository.getByIpAddress(ipAddress);

        if (Objects.isNull(ipAddressInfo)) {
            ipAddressInfo = new IpAddressEmailMessagesEntity();

            ipAddressInfo.setIpAddress(ipAddress);
            ipAddressInfo.setCountWriteMessage(0);
            ipAddressInfo.setFirstWriteMessage(LocalDateTime.now());
        }

        LocalDateTime timeInIp = ipAddressInfo.getFirstWriteMessage().plusHours(1);
        if (timeInIp.isBefore(LocalDateTime.now())) {
            ipAddressInfo.setCountWriteMessage(0);
            ipAddressInfo.setFirstWriteMessage(LocalDateTime.now());
        }

        ipAddressInfo.setCountWriteMessage(ipAddressInfo.getCountWriteMessage() + 1);
        ipAddressEmailMessagesRepository.save(ipAddressInfo);

        AuthAssert.canWriteMessage(ipAddressInfo.getCountWriteMessage() < 3, "Вы пытались отправить слишком много сообщений на почтовые ящики, попробуйте повторить попытку позже");

        UserEntity user = userRepository.findByUsernameAndPassword(userAuthorizationDto.getUsername(), Sha256PasswordUtil.hashCode(userAuthorizationDto.getPassword()));

        if (Objects.isNull(user)) {
            throw new BadRequestException("Неверынй логин или пароль, попробуйте ещё раз");
        }

        if (!user.getEmail().getIsConfirmed()) {
            throw new BadRequestException("Ваша почта не подтверждена");
        }

        TokenEntity tokenFromDB = TokenUtil.generateToken(user, userAgent);
        tokenFromDB = tokenRepository.save(tokenFromDB);

        IpAddressEntity ipAddressFromDB = new IpAddressEntity();
        ipAddressFromDB.setToken(tokenFromDB);
        ipAddressFromDB.setIpAddress(ipAddress);
        ipAddressFromDB.setIsConfirmed(false);
        ipAddressRepository.save(ipAddressFromDB);

        ConfirmIpAddressEntity confirmIpAddressFromDB = new ConfirmIpAddressEntity();
        confirmIpAddressFromDB.setIpAddress(ipAddressFromDB);
        confirmIpAddressFromDB.setNumberOfConfirmationAttempts(0);
        confirmIpAddressFromDB.setConfirmCode(Arrays.stream(NumberUtil.getArrRandomInt(8)).mapToObj(String::valueOf).collect(Collectors.joining("")));
        confirmIpAddressRepository.save(confirmIpAddressFromDB);

        alertsService.writeConfirmIpAddressCode(user.getUsername(), user.getEmail().getEmail(), ipAddress, userAgent, confirmIpAddressFromDB.getConfirmCode());

        TokenDto tokenFromUser = new TokenDto();
        tokenFromUser.setToken(tokenFromDB.getToken());
        return tokenFromUser;
    }

    public void acceptAuthorization(CodeDto code, String ipAddress, String userAgent) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        ConfirmIpAddressEntity confirmIp = confirmIpAddressRepository.findByIpAddressTokenUserUsernameAndIpAddressIpAddress(username, ipAddress);

        if (confirmIp.getConfirmCode().equals(code.getCode())) {
            IpAddressEntity ipAddressEntity = ipAddressRepository.findByIpAddressAndTokenUserUsername(ipAddress, username);

            if (Objects.isNull(ipAddressEntity)) {
                throw new BadRequestException("Ошибка подтверждения IP адреса");
            }

            ipAddressEntity.setIsConfirmed(true);
            ipAddressRepository.save(ipAddressEntity);

            confirmIpAddressRepository.delete(confirmIp);
            return;
        }

        confirmIp.setNumberOfConfirmationAttempts(confirmIp.getNumberOfConfirmationAttempts() + 1);
        confirmIpAddressRepository.save(confirmIp);

        if (confirmIp.getNumberOfConfirmationAttempts() > 4) {
            confirmIpAddressRepository.delete(confirmIp);
            ipAddressRepository.delete(ipAddressRepository.findByIpAddressAndTokenUserUsername(ipAddress, username));
            tokenRepository.delete(tokenRepository.getByUserUsername(username));

            throw new BadRequestException("Вы потратили все попытки, попробуйте войти ещё раз");
        }

        throw new BadRequestException("Код введён неверно, попробуйте ещё раз");
    }

    public void repeatAuthorizationCode(String ipAddress, String userAgent) {
        IpAddressEmailMessagesEntity ipAddressInfo = ipAddressEmailMessagesRepository.getByIpAddress(ipAddress);

        if (Objects.isNull(ipAddressInfo)) {
            ipAddressInfo = new IpAddressEmailMessagesEntity();

            ipAddressInfo.setIpAddress(ipAddress);
            ipAddressInfo.setCountWriteMessage(0);
            ipAddressInfo.setFirstWriteMessage(LocalDateTime.now());
        }

        LocalDateTime timeInIp = ipAddressInfo.getFirstWriteMessage().plusHours(1);
        if (timeInIp.isBefore(LocalDateTime.now())) {
            ipAddressInfo.setCountWriteMessage(0);
            ipAddressInfo.setFirstWriteMessage(LocalDateTime.now());
        }

        ipAddressInfo.setCountWriteMessage(ipAddressInfo.getCountWriteMessage() + 1);

        ipAddressEmailMessagesRepository.save(ipAddressInfo);

        AuthAssert.canWriteMessage(ipAddressInfo.getCountWriteMessage() < 3, "Вы пытались отправить слишком много сообщений на почтовые ящики, попробуйте повторить попытку позже");

        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserEntity user = userRepository.findByUsername(username);

        ConfirmIpAddressEntity confirmIp = confirmIpAddressRepository.findByIpAddressTokenUserUsernameAndIpAddressIpAddress(username, ipAddress);
        if (Objects.nonNull(confirmIp)) {
            alertsService.writeConfirmIpAddressCode(user.getUsername(), user.getEmail().getEmail(), ipAddress, userAgent, confirmIp.getConfirmCode());
        } else {
            throw new BadRequestException("Вам не нужно подтверждать вход");
        }
    }

    public void addCurrentIp(String ipAddress, String userAgent) {
        IpAddressEmailMessagesEntity ipAddressInfo = ipAddressEmailMessagesRepository.getByIpAddress(ipAddress);

        if (Objects.isNull(ipAddressInfo)) {
            ipAddressInfo = new IpAddressEmailMessagesEntity();

            ipAddressInfo.setIpAddress(ipAddress);
            ipAddressInfo.setCountWriteMessage(0);
            ipAddressInfo.setFirstWriteMessage(LocalDateTime.now());
        }

        LocalDateTime timeInIp = ipAddressInfo.getFirstWriteMessage().plusHours(1);
        if (timeInIp.isBefore(LocalDateTime.now())) {
            ipAddressInfo.setCountWriteMessage(0);
            ipAddressInfo.setFirstWriteMessage(LocalDateTime.now());
        }

        ipAddressInfo.setCountWriteMessage(ipAddressInfo.getCountWriteMessage() + 1);

        ipAddressEmailMessagesRepository.save(ipAddressInfo);

        AuthAssert.canWriteMessage(ipAddressInfo.getCountWriteMessage() < 3, "Вы пытались отправить слишком много сообщений на почтовые ящики, попробуйте повторить попытку позже");

        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TokenEntity token = tokenRepository.getByUserUsername(username);

        if (Objects.nonNull(ipAddressRepository.findByIpAddressAndTokenToken(ipAddress, token.getToken()))) {
            throw new BadRequestException("Вам не нужно доабвлять данный IP");
        }

        IpAddressEntity ipAddressFromDB = new IpAddressEntity();
        ipAddressFromDB.setToken(token);
        ipAddressFromDB.setIpAddress(ipAddress);
        ipAddressFromDB.setIsConfirmed(false);
        ipAddressRepository.save(ipAddressFromDB);

        ConfirmIpAddressEntity confirmIpAddressFromDB = new ConfirmIpAddressEntity();
        confirmIpAddressFromDB.setIpAddress(ipAddressFromDB);
        confirmIpAddressFromDB.setNumberOfConfirmationAttempts(0);
        confirmIpAddressFromDB.setConfirmCode(Arrays.stream(NumberUtil.getArrRandomInt(8)).mapToObj(String::valueOf).collect(Collectors.joining("")));
        confirmIpAddressRepository.save(confirmIpAddressFromDB);

        UserEntity user = userRepository.findByUsername(username);

        alertsService.writeAddIpCode(user.getUsername(), user.getEmail().getEmail(), ipAddress, userAgent, confirmIpAddressFromDB.getConfirmCode());
    }

    public void acceptCurrentIp(CodeDto code, String ipAddress, String userAgent) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        ConfirmIpAddressEntity confirmIp = confirmIpAddressRepository.findByIpAddressTokenUserUsernameAndIpAddressIpAddress(username, ipAddress);

        if (confirmIp.getConfirmCode().equals(code.getCode())) {
            IpAddressEntity ipAddressEntity = ipAddressRepository.findByIpAddressAndTokenUserUsername(ipAddress, username);

            if (Objects.isNull(ipAddressEntity)) {
                throw new BadRequestException("Ошибка подтверждения IP адреса");
            }

            ipAddressEntity.setIsConfirmed(true);
            ipAddressRepository.save(ipAddressEntity);

            confirmIpAddressRepository.delete(confirmIp);
            return;
        }

        confirmIp.setNumberOfConfirmationAttempts(confirmIp.getNumberOfConfirmationAttempts() + 1);
        confirmIpAddressRepository.save(confirmIp);

        if (confirmIp.getNumberOfConfirmationAttempts() > 4) {
            confirmIpAddressRepository.delete(confirmIp);
            ipAddressRepository.delete(ipAddressRepository.findByIpAddressAndTokenUserUsername(ipAddress, username));

            throw new BadRequestException("Вы потратили все попытки, попробуйте войти ещё раз");
        }

        throw new BadRequestException("Код введён неверно, попробуйте ещё раз");
    }

    public void repeatIpAddressCode(String ipAddress, String userAgent) {
        IpAddressEmailMessagesEntity ipAddressInfo = ipAddressEmailMessagesRepository.getByIpAddress(ipAddress);

        if (Objects.isNull(ipAddressInfo)) {
            ipAddressInfo = new IpAddressEmailMessagesEntity();

            ipAddressInfo.setIpAddress(ipAddress);
            ipAddressInfo.setCountWriteMessage(0);
            ipAddressInfo.setFirstWriteMessage(LocalDateTime.now());
        }

        LocalDateTime timeInIp = ipAddressInfo.getFirstWriteMessage().plusHours(1);
        if (timeInIp.isBefore(LocalDateTime.now())) {
            ipAddressInfo.setCountWriteMessage(0);
            ipAddressInfo.setFirstWriteMessage(LocalDateTime.now());
        }

        ipAddressInfo.setCountWriteMessage(ipAddressInfo.getCountWriteMessage() + 1);

        ipAddressEmailMessagesRepository.save(ipAddressInfo);

        AuthAssert.canWriteMessage(ipAddressInfo.getCountWriteMessage() < 3, "Вы пытались отправить слишком много сообщений на почтовые ящики, попробуйте повторить попытку позже");

        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserEntity user = userRepository.findByUsername(username);

        ConfirmIpAddressEntity confirmIp = confirmIpAddressRepository.findByIpAddressTokenUserUsernameAndIpAddressIpAddress(username, ipAddress);
        if (Objects.nonNull(confirmIp)) {
            alertsService.writeAddIpCode(user.getUsername(), user.getEmail().getEmail(), ipAddress, userAgent, confirmIp.getConfirmCode());
        } else {
            throw new BadRequestException("Вам не нужно подтверждать вход");
        }
    }

    public void removeIpAddress(RemoveIpDto removeIp, String ipAddress, String userAgent) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        IpAddressEntity ipAddressEntity = ipAddressRepository.findByIpAddressAndTokenUserUsername(removeIp.getIp(), username);

        if (Objects.isNull(ipAddressEntity)) {
            throw new BadRequestException("Данно IP адреса не существует");
        }

        ConfirmIpAddressEntity confirmIp = confirmIpAddressRepository.findByIpAddressTokenUserUsernameAndIpAddressIpAddress(username, ipAddress);
        confirmIpAddressRepository.delete(confirmIp);
        ipAddressRepository.delete(ipAddressEntity);
    }

    public void removeToken(TokenDto tokenDto, String ipAddress, String userAgent) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TokenEntity token = tokenRepository.getByUserUsername(tokenDto.getToken());

        if (Objects.isNull(token)) {
            throw new BadRequestException("Данно Token адреса не существует");
        }

        confirmIpAddressRepository.removeAllByIpAddressTokenToken(token.getToken());
        ipAddressRepository.removeAllByTokenToken(token.getToken());
        tokenRepository.delete(tokenRepository.getByUserUsername(tokenDto.getToken()));
    }

    public boolean ipCanUseThisToken(String ip, String token) {
        IpAddressEntity ipAddress = ipAddressRepository.findByIpAddressAndTokenToken(ip, token);
        if (Objects.isNull(ipAddress)) {
            return false;
        }
        return ipAddress.getIsConfirmed();
    }

    public String getUsernameByToken(String token) {
        return tokenRepository.getByToken(token).getUser().getUsername();
    }
}
