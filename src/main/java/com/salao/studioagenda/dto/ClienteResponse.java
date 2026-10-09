package com.salao.studioagenda.dto;

import com.salao.studioagenda.model.Cliente;

public record ClienteResponse(Long id, String nome, String telefone, String email) {

    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getTelefone(), cliente.getEmail());
    }
}
