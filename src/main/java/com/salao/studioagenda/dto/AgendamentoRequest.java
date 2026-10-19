package com.salao.studioagenda.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Set;

public record AgendamentoRequest(
        @NotNull Long clienteId,
        @NotNull Long profissionalId,
        @NotEmpty Set<Long> servicoIds,
        @NotNull @Future @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime dataHoraInicio
) {
}
