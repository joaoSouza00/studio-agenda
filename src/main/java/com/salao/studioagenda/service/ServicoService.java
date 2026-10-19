package com.salao.studioagenda.service;

import com.salao.studioagenda.dto.ServicoRequest;
import com.salao.studioagenda.dto.ServicoResponse;
import com.salao.studioagenda.exception.RecursoNaoEncontradoException;
import com.salao.studioagenda.model.Servico;
import com.salao.studioagenda.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicoService {

    private final ServicoRepository servicoRepository;

    public List<ServicoResponse> listarTodos() {
        return servicoRepository.findAll().stream()
                .map(ServicoResponse::from)
                .toList();
    }

    public ServicoResponse buscarPorId(Long id) {
        return ServicoResponse.from(buscarEntidade(id));
    }

    @Transactional
    public ServicoResponse criar(ServicoRequest request) {
        Servico servico = new Servico();
        aplicarDados(servico, request);
        return ServicoResponse.from(servicoRepository.save(servico));
    }

    @Transactional
    public ServicoResponse atualizar(Long id, ServicoRequest request) {
        Servico servico = buscarEntidade(id);
        aplicarDados(servico, request);
        return ServicoResponse.from(servicoRepository.save(servico));
    }

    @Transactional
    public void excluir(Long id) {
        servicoRepository.delete(buscarEntidade(id));
    }

    private Servico buscarEntidade(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço", id));
    }

    private void aplicarDados(Servico servico, ServicoRequest request) {
        servico.setNome(request.nome());
        servico.setDuracaoMinutos(request.duracaoMinutos());
        servico.setPreco(request.preco());
    }
}
