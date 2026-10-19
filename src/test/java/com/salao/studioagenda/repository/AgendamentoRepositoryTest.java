package com.salao.studioagenda.repository;

import com.salao.studioagenda.model.Agendamento;
import com.salao.studioagenda.model.Cliente;
import com.salao.studioagenda.model.Profissional;
import com.salao.studioagenda.model.Servico;
import com.salao.studioagenda.model.StatusAgendamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AgendamentoRepositoryTest {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    private Cliente cliente;
    private Profissional ana;
    private Profissional bruno;
    private Servico corte;
    private LocalDateTime dezHoras;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setNome("Maria");
        cliente.setTelefone("11999990000");
        clienteRepository.save(cliente);

        ana = salvarProfissional("Ana");
        bruno = salvarProfissional("Bruno");

        corte = new Servico();
        corte.setNome("Corte");
        corte.setDuracaoMinutos(60);
        corte.setPreco(new BigDecimal("50.00"));
        servicoRepository.save(corte);

        dezHoras = LocalDateTime.of(2030, 5, 10, 10, 0);
    }

    @Test
    void deveEncontrarConflitoQuandoHorariosSeSobrepoem() {
        salvarAgendamento(ana, dezHoras, StatusAgendamento.AGENDADO);

        List<Agendamento> conflitos = buscarConflitos(ana, dezHoras.plusMinutes(30), dezHoras.plusMinutes(90), null);

        assertThat(conflitos).hasSize(1);
    }

    @Test
    void deveEncontrarConflitoQuandoNovoHorarioEstaDentroDoExistente() {
        salvarAgendamento(ana, dezHoras, StatusAgendamento.AGENDADO);

        List<Agendamento> conflitos = buscarConflitos(ana, dezHoras.plusMinutes(15), dezHoras.plusMinutes(45), null);

        assertThat(conflitos).hasSize(1);
    }

    @Test
    void naoDeveHaverConflitoQuandoHorariosApenasEncostam() {
        salvarAgendamento(ana, dezHoras, StatusAgendamento.AGENDADO);

        assertThat(buscarConflitos(ana, dezHoras.plusMinutes(60), dezHoras.plusMinutes(120), null)).isEmpty();
        assertThat(buscarConflitos(ana, dezHoras.minusMinutes(60), dezHoras, null)).isEmpty();
    }

    @Test
    void naoDeveHaverConflitoComOutroProfissional() {
        salvarAgendamento(ana, dezHoras, StatusAgendamento.AGENDADO);

        assertThat(buscarConflitos(bruno, dezHoras, dezHoras.plusMinutes(60), null)).isEmpty();
    }

    @Test
    void deveIgnorarAgendamentosCancelados() {
        salvarAgendamento(ana, dezHoras, StatusAgendamento.CANCELADO);

        assertThat(buscarConflitos(ana, dezHoras, dezHoras.plusMinutes(60), null)).isEmpty();
    }

    @Test
    void deveConsiderarAgendamentosConcluidos() {
        salvarAgendamento(ana, dezHoras, StatusAgendamento.CONCLUIDO);

        assertThat(buscarConflitos(ana, dezHoras, dezHoras.plusMinutes(60), null)).hasSize(1);
    }

    @Test
    void deveIgnorarOProprioAgendamentoNaEdicao() {
        Agendamento existente = salvarAgendamento(ana, dezHoras, StatusAgendamento.AGENDADO);

        assertThat(buscarConflitos(ana, dezHoras.plusMinutes(30), dezHoras.plusMinutes(90), existente.getId())).isEmpty();
    }

    @Test
    void existsDeveIndicarVinculosDoAgendamento() {
        salvarAgendamento(ana, dezHoras, StatusAgendamento.AGENDADO);

        assertThat(agendamentoRepository.existsByClienteId(cliente.getId())).isTrue();
        assertThat(agendamentoRepository.existsByProfissionalId(ana.getId())).isTrue();
        assertThat(agendamentoRepository.existsByProfissionalId(bruno.getId())).isFalse();
        assertThat(agendamentoRepository.existsByServicosId(corte.getId())).isTrue();
    }

    private List<Agendamento> buscarConflitos(Profissional profissional, LocalDateTime inicio, LocalDateTime fim, Long agendamentoId) {
        return agendamentoRepository.buscarConflitosDeHorario(profissional.getId(), inicio, fim, agendamentoId, StatusAgendamento.CANCELADO);
    }

    private Agendamento salvarAgendamento(Profissional profissional, LocalDateTime inicio, StatusAgendamento status) {
        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setProfissional(profissional);
        agendamento.setServicos(new HashSet<>(Set.of(corte)));
        agendamento.setDataHoraInicio(inicio);
        agendamento.recalcularDataHoraFim();
        agendamento.setStatus(status);
        return agendamentoRepository.saveAndFlush(agendamento);
    }

    private Profissional salvarProfissional(String nome) {
        Profissional profissional = new Profissional();
        profissional.setNome(nome);
        profissional.setEspecialidade("Cabeleireira");
        return profissionalRepository.save(profissional);
    }
}
