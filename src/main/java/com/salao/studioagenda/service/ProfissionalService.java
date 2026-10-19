package com.salao.studioagenda.service;

import com.salao.studioagenda.dto.ProfissionalRequest;
import com.salao.studioagenda.dto.ProfissionalResponse;
import com.salao.studioagenda.exception.RecursoNaoEncontradoException;
import com.salao.studioagenda.exception.RegraNegocioException;
import com.salao.studioagenda.model.Profissional;
import com.salao.studioagenda.repository.AgendamentoRepository;
import com.salao.studioagenda.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final AgendamentoRepository agendamentoRepository;

    public List<ProfissionalResponse> listarTodos() {
        return profissionalRepository.findAll().stream()
                .map(ProfissionalResponse::from)
                .toList();
    }

    public ProfissionalResponse buscarPorId(Long id) {
        return ProfissionalResponse.from(buscarEntidade(id));
    }

    @Transactional
    public ProfissionalResponse criar(ProfissionalRequest request) {
        Profissional profissional = new Profissional();
        aplicarDados(profissional, request);
        return ProfissionalResponse.from(profissionalRepository.save(profissional));
    }

    @Transactional
    public ProfissionalResponse atualizar(Long id, ProfissionalRequest request) {
        Profissional profissional = buscarEntidade(id);
        aplicarDados(profissional, request);
        return ProfissionalResponse.from(profissionalRepository.save(profissional));
    }

    @Transactional
    public void excluir(Long id) {
        Profissional profissional = buscarEntidade(id);
        if (agendamentoRepository.existsByProfissionalId(id)) {
            throw new RegraNegocioException("Não é possível excluir o profissional " + profissional.getNome() + " porque ele possui agendamentos");
        }
        profissionalRepository.delete(profissional);
    }

    private Profissional buscarEntidade(Long id) {
        return profissionalRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional", id));
    }

    private void aplicarDados(Profissional profissional, ProfissionalRequest request) {
        profissional.setNome(request.nome());
        profissional.setEspecialidade(request.especialidade());
    }
}
