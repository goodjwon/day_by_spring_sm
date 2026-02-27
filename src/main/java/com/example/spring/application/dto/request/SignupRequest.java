package com.example.spring.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "{validation.name.required}")
    @Size(min = 2, max = 50)
    private String name;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 4, message = "{validation.password.min4}")
    private String password;

}