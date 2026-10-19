package com.salao.studioagenda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfissionalRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Size(max = 60) String especialidade
) {
}
