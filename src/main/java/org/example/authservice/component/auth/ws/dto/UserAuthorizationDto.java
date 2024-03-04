package org.example.authservice.component.auth.ws.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class UserAuthorizationDto {

    @NotBlank(message = "Поле username не может быть пустым")
    private String username;

    @NotBlank(message = "Поле password не может быть пустым")
    private String password;

}
