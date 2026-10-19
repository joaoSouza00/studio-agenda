package com.salao.studioagenda.service;

import com.salao.studioagenda.dto.AgendamentoRequest;
import com.salao.studioagenda.dto.AgendamentoResponse;
import com.salao.studioagenda.exception.ConflitoHorarioException;
import com.salao.studioagenda.exception.RecursoNaoEncontradoException;
import com.salao.studioagenda.exception.RegraNegocioException;
import com.salao.studioagenda.model.Agendamento;
import com.salao.studioagenda.model.Cliente;
import com.salao.studioagenda.model.Profissional;
import com.salao.studioagenda.model.Servico;
import com.salao.studioagenda.model.StatusAgendamento;
import com.salao.studioagenda.repository.AgendamentoRepository;
import com.salao.studioagenda.repository.ClienteRepository;
import com.salao.studioagenda.repository.ProfissionalRepository;
import com.salao.studioagenda.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ServicoRepository servicoRepository;

    public List<AgendamentoResponse> listarTodos() {
        return agendamentoRepository.findAll().stream()
                .map(AgendamentoResponse::from)
                .toList();
    }

    public List<AgendamentoResponse> listarPorCliente(Long clienteId) {
        return agendamentoRepository.findByClienteId(clienteId).stream()
                .map(AgendamentoResponse::from)
                .toList();
    }

    public List<AgendamentoResponse> listarPorProfissional(Long profissionalId) {
        return agendamentoRepository.findByProfissionalId(profissionalId).stream()
                .map(AgendamentoResponse::from)
                .toList();
    }

    public AgendamentoResponse buscarPorId(Long id) {
        return AgendamentoResponse.from(buscarEntidade(id));
    }

    @Transactional
    public AgendamentoResponse criar(AgendamentoRequest request) {
        Agendamento agendamento = new Agendamento();
        aplicarDados(agendamento, request);
        validarConflito(agendamento);
        return AgendamentoResponse.from(agendamentoRepository.save(agendamento));
    }

    @Transactional
    public AgendamentoResponse atualizar(Long id, AgendamentoRequest request) {
        Agendamento agendamento = buscarEntidade(id);
        validarStatusAgendado(agendamento);
        aplicarDados(agendamento, request);
        validarConflito(agendamento);
        return AgendamentoResponse.from(agendamentoRepository.save(agendamento));
    }

    @Transactional
    public AgendamentoResponse cancelar(Long id) {
        return alterarStatus(id, StatusAgendamento.CANCELADO);
    }

    @Transactional
    public AgendamentoResponse concluir(Long id) {
        return alterarStatus(id, StatusAgendamento.CONCLUIDO);
    }

    private AgendamentoResponse alterarStatus(Long id, StatusAgendamento novoStatus) {
        Agendamento agendamento = buscarEntidade(id);
        validarStatusAgendado(agendamento);
        agendamento.setStatus(novoStatus);
        return AgendamentoResponse.from(agendamentoRepository.save(agendamento));
    }

    private void aplicarDados(Agendamento agendamento, AgendamentoRequest request) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", request.clienteId()));
        Profissional profissional = profissionalRepository.findById(request.profissionalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional", request.profissionalId()));

        agendamento.setCliente(cliente);
        agendamento.setProfissional(profissional);
        agendamento.setServicos(buscarServicos(request.servicoIds()));
        agendamento.setDataHoraInicio(request.dataHoraInicio());
        agendamento.recalcularDataHoraFim();
    }

    private Set<Servico> buscarServicos(Set<Long> servicoIds) {
        Set<Servico> servicos = new HashSet<>(servicoRepository.findAllById(servicoIds));
        if (servicos.size() != servicoIds.size()) {
            throw new RecursoNaoEncontradoException("Um ou mais serviços informados não foram encontrados");
        }
        return servicos;
    }

    private void validarConflito(Agendamento agendamento) {
        List<Agendamento> conflitos = agendamentoRepository.buscarConflitosDeHorario(
                agendamento.getProfissional().getId(),
                agendamento.getDataHoraInicio(),
                agendamento.getDataHoraFim(),
                agendamento.getId(),
                StatusAgendamento.CANCELADO
        );
        if (!conflitos.isEmpty()) {
            throw new ConflitoHorarioException(
                    "O profissional " + agendamento.getProfissional().getNome() + " já possui agendamento neste horário");
        }
    }

    private void validarStatusAgendado(Agendamento agendamento) {
        if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
            throw new RegraNegocioException(
                    "Apenas agendamentos com status AGENDADO podem ser alterados. Status atual: " + agendamento.getStatus());
        }
    }

    private Agendamento buscarEntidade(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento", id));
    }
}
