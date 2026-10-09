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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private Cliente cliente;
    private Profissional profissional;
    private Servico corte;
    private Servico escova;
    private LocalDateTime inicio;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Maria");

        profissional = new Profissional();
        profissional.setId(1L);
        profissional.setNome("Ana");

        corte = criarServico(1L, "Corte", 30, "50.00");
        escova = criarServico(2L, "Escova", 45, "40.00");

        inicio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
    }

    @Test
    void criarDeveCalcularHorarioFimEValorTotal() {
        AgendamentoRequest request = new AgendamentoRequest(1L, 1L, Set.of(1L, 2L), inicio);
        mockarEntidadesDoRequest();
        when(agendamentoRepository.buscarConflitosDeHorario(eq(1L), eq(inicio), eq(inicio.plusMinutes(75)), isNull(), eq(StatusAgendamento.CANCELADO)))
                .thenReturn(List.of());
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgendamentoResponse response = agendamentoService.criar(request);

        assertThat(response.dataHoraFim()).isEqualTo(inicio.plusMinutes(75));
        assertThat(response.duracaoTotalMinutos()).isEqualTo(75);
        assertThat(response.valorTotal()).isEqualByComparingTo("90.00");
        assertThat(response.status()).isEqualTo(StatusAgendamento.AGENDADO);
        assertThat(response.servicos()).hasSize(2);
    }

    @Test
    void criarDeveLancarExcecaoQuandoHouverConflitoDeHorario() {
        AgendamentoRequest request = new AgendamentoRequest(1L, 1L, Set.of(1L, 2L), inicio);
        mockarEntidadesDoRequest();
        when(agendamentoRepository.buscarConflitosDeHorario(any(), any(), any(), any(), any()))
                .thenReturn(List.of(new Agendamento()));

        assertThatThrownBy(() -> agendamentoService.criar(request))
                .isInstanceOf(ConflitoHorarioException.class);
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void criarDeveLancarExcecaoQuandoServicoNaoExistir() {
        AgendamentoRequest request = new AgendamentoRequest(1L, 1L, Set.of(1L, 99L), inicio);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(servicoRepository.findAllById(Set.of(1L, 99L))).thenReturn(List.of(corte));

        assertThatThrownBy(() -> agendamentoService.criar(request))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void criarDeveLancarExcecaoQuandoClienteNaoExistir() {
        AgendamentoRequest request = new AgendamentoRequest(99L, 1L, Set.of(1L), inicio);
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendamentoService.criar(request))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void atualizarDeveIgnorarOProprioAgendamentoNaVerificacaoDeConflito() {
        Agendamento existente = criarAgendamentoExistente(StatusAgendamento.AGENDADO);
        AgendamentoRequest request = new AgendamentoRequest(1L, 1L, Set.of(1L, 2L), inicio.plusMinutes(15));
        when(agendamentoRepository.findById(10L)).thenReturn(Optional.of(existente));
        mockarEntidadesDoRequest();
        when(agendamentoRepository.buscarConflitosDeHorario(eq(1L), any(), any(), eq(10L), eq(StatusAgendamento.CANCELADO)))
                .thenReturn(List.of());
        when(agendamentoRepository.save(existente)).thenReturn(existente);

        AgendamentoResponse response = agendamentoService.atualizar(10L, request);

        assertThat(response.dataHoraInicio()).isEqualTo(inicio.plusMinutes(15));
        assertThat(response.dataHoraFim()).isEqualTo(inicio.plusMinutes(90));
    }

    @Test
    void cancelarDeveAlterarStatusParaCancelado() {
        Agendamento existente = criarAgendamentoExistente(StatusAgendamento.AGENDADO);
        when(agendamentoRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(agendamentoRepository.save(existente)).thenReturn(existente);

        AgendamentoResponse response = agendamentoService.cancelar(10L);

        assertThat(response.status()).isEqualTo(StatusAgendamento.CANCELADO);
    }

    @Test
    void concluirDeveLancarExcecaoQuandoAgendamentoJaEstiverCancelado() {
        Agendamento existente = criarAgendamentoExistente(StatusAgendamento.CANCELADO);
        when(agendamentoRepository.findById(10L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> agendamentoService.concluir(10L))
                .isInstanceOf(RegraNegocioException.class);
        verify(agendamentoRepository, never()).save(any());
    }

    private void mockarEntidadesDoRequest() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(servicoRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(corte, escova));
    }

    private Agendamento criarAgendamentoExistente(StatusAgendamento status) {
        Agendamento agendamento = new Agendamento();
        agendamento.setId(10L);
        agendamento.setCliente(cliente);
        agendamento.setProfissional(profissional);
        agendamento.setServicos(new HashSet<>(Set.of(corte, escova)));
        agendamento.setDataHoraInicio(inicio);
        agendamento.recalcularDataHoraFim();
        agendamento.setStatus(status);
        return agendamento;
    }

    private Servico criarServico(Long id, String nome, int duracao, String preco) {
        Servico servico = new Servico();
        servico.setId(id);
        servico.setNome(nome);
        servico.setDuracaoMinutos(duracao);
        servico.setPreco(new BigDecimal(preco));
        return servico;
    }
}
