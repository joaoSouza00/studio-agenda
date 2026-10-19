package com.salao.studioagenda.dto;

import com.salao.studioagenda.model.Profissional;

public record ProfissionalResponse(Long id, String nome, String especialidade) {

    public static ProfissionalResponse from(Profissional profissional) {
        return new ProfissionalResponse(profissional.getId(), profissional.getNome(), profissional.getEspecialidade());
    }
}
