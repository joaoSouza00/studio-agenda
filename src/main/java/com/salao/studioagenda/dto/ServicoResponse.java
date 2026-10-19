package com.salao.studioagenda.dto;

import com.salao.studioagenda.model.Servico;

import java.math.BigDecimal;

public record ServicoResponse(Long id, String nome, Integer duracaoMinutos, BigDecimal preco) {

    public static ServicoResponse from(Servico servico) {
        return new ServicoResponse(servico.getId(), servico.getNome(), servico.getDuracaoMinutos(), servico.getPreco());
    }
}
