package com.enzobf.reactivebingo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlayerRequest(
        @NotBlank(message = "O nome e obrigatorio")
        @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres")
        String name,

        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "O e-mail deve ser valido")
        String email
) {
}
