package com.salao.studioagenda.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Size(max = 20) String telefone,
        @Email @Size(max = 100) String email
) {
}
