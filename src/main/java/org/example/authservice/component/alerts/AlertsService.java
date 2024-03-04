package org.example.authservice.component.alerts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AlertsService {

    @Autowired
    private JavaMailSender mailSender;

    public void writeRegistrationCode(String username, String email, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("alex.verdene@yandex.ru");
        msg.setTo(email);
        msg.setSubject("Регистрация в приложении MyChild");
        msg.setText("Здравсвтуйте, " + username + "!\n" +
                "Ваш код для подтверждения регистрации: " + code +
                "\n\nC уважением,\nПоддежка приложения MyChild");
        mailSender.send(msg);
    }

    public void writeConfirmIpAddressCode(String username, String email, String ipAddress, String userAgent, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("alex.verdene@yandex.ru");
        msg.setTo(email);
        msg.setSubject("Подтверждение входа MyChild");
        msg.setText("Здравсвтуйте, " + username + "!\n" +
                "Мы зафиксировали попытку входа в аккаунт IP адресом: " + ipAddress +
                "\nИ userAgent " + userAgent +
                "\n\nЕсли это вы, то ваш код подтверждения IP адреса: " + code +
                "\n\nC уважением,\nПоддежка приложения MyChild");
        mailSender.send(msg);
    }

    public void writeAddIpCode(String username, String email, String ipAddress, String userAgent, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("alex.verdene@yandex.ru");
        msg.setTo(email);
        msg.setSubject("Вход с нового места MyChild");
        msg.setText("Здравсвтуйте, " + username + "!\n" +
                "Мы зафиксировали попытку входа в с нового IP адреса: " + ipAddress +
                "\nИ userAgent " + userAgent +
                "\n\nЕсли это вы, то ваш код подтверждения IP адреса: " + code +
                "\n\nC уважением,\nПоддежка приложения MyChild");
        mailSender.send(msg);
    }
}
