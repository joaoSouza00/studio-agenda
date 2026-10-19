package com.salao.studioagenda.dto;

import com.salao.studioagenda.model.Agendamento;
import com.salao.studioagenda.model.StatusAgendamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record AgendamentoResponse(
        Long id,
        Long clienteId,
        String clienteNome,
        Long profissionalId,
        String profissionalNome,
        List<ServicoResponse> servicos,
        LocalDateTime dataHoraInicio,
        LocalDateTime dataHoraFim,
        int duracaoTotalMinutos,
        BigDecimal valorTotal,
        StatusAgendamento status
) {

    public static AgendamentoResponse from(Agendamento agendamento) {
        List<ServicoResponse> servicos = agendamento.getServicos().stream()
                .map(ServicoResponse::from)
                .sorted(Comparator.comparing(ServicoResponse::nome))
                .toList();

        return new AgendamentoResponse(
                agendamento.getId(),
                agendamento.getCliente().getId(),
                agendamento.getCliente().getNome(),
                agendamento.getProfissional().getId(),
                agendamento.getProfissional().getNome(),
                servicos,
                agendamento.getDataHoraInicio(),
                agendamento.getDataHoraFim(),
                agendamento.calcularDuracaoTotalMinutos(),
                agendamento.calcularValorTotal(),
                agendamento.getStatus()
        );
    }
}
