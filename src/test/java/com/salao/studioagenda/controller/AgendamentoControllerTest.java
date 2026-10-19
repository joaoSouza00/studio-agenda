package com.salao.studioagenda.controller;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@Transactional
class AgendamentoControllerTest {

    private static final DateTimeFormatter FORMATO_INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    private MockMvc mockMvc;
    private Cliente cliente;
    private Profissional profissional;
    private Servico corte;
    private Servico escova;
    private LocalDateTime inicio;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        cliente = new Cliente();
        cliente.setNome("Maria Silva");
        cliente.setTelefone("11999990000");
        clienteRepository.save(cliente);

        profissional = new Profissional();
        profissional.setNome("Ana Souza");
        profissional.setEspecialidade("Cabeleireira");
        profissionalRepository.save(profissional);

        corte = salvarServico("Corte", 30, "50.00");
        escova = salvarServico("Escova", 45, "40.00");

        inicio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
    }

    @Test
    void listaDeveExibirAgendamentos() throws Exception {
        salvarAgendamento(inicio, StatusAgendamento.AGENDADO);

        mockMvc.perform(get("/agendamentos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Maria Silva")))
                .andExpect(content().string(containsString("Corte, Escova")))
                .andExpect(content().string(containsString("R$ 90,00")));
    }

    @Test
    void formularioNovoDeveListarOpcoes() throws Exception {
        mockMvc.perform(get("/agendamentos/novo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Maria Silva")))
                .andExpect(content().string(containsString("Ana Souza (Cabeleireira)")))
                .andExpect(content().string(containsString("Escova (45 min, R$ 40,00)")));
    }

    @Test
    void deveCriarAgendamentoCalculandoHorarioFim() throws Exception {
        mockMvc.perform(post("/agendamentos")
                        .param("clienteId", cliente.getId().toString())
                        .param("profissionalId", profissional.getId().toString())
                        .param("servicoIds", corte.getId().toString(), escova.getId().toString())
                        .param("dataHoraInicio", inicio.format(FORMATO_INPUT)))
                .andExpect(redirectedUrl("/agendamentos"))
                .andExpect(flash().attribute("sucesso", "Agendamento criado com sucesso"));

        List<Agendamento> agendamentos = agendamentoRepository.findAll();
        assertThat(agendamentos).hasSize(1);
        assertThat(agendamentos.getFirst().getDataHoraFim()).isEqualTo(inicio.plusMinutes(75));
    }

    @Test
    void naoDeveCriarAgendamentoComConflitoDeHorario() throws Exception {
        salvarAgendamento(inicio, StatusAgendamento.AGENDADO);

        mockMvc.perform(post("/agendamentos")
                        .param("clienteId", cliente.getId().toString())
                        .param("profissionalId", profissional.getId().toString())
                        .param("servicoIds", corte.getId().toString())
                        .param("dataHoraInicio", inicio.plusMinutes(30).format(FORMATO_INPUT)))
                .andExpect(status().isOk())
                .andExpect(view().name("agendamentos/form"))
                .andExpect(content().string(containsString("já possui agendamento neste horário")));

        assertThat(agendamentoRepository.count()).isEqualTo(1);
    }

    @Test
    void devePermitirAgendamentoNoHorarioDeUmCancelado() throws Exception {
        salvarAgendamento(inicio, StatusAgendamento.CANCELADO);

        mockMvc.perform(post("/agendamentos")
                        .param("clienteId", cliente.getId().toString())
                        .param("profissionalId", profissional.getId().toString())
                        .param("servicoIds", corte.getId().toString())
                        .param("dataHoraInicio", inicio.format(FORMATO_INPUT)))
                .andExpect(redirectedUrl("/agendamentos"));

        assertThat(agendamentoRepository.count()).isEqualTo(2);
    }

    @Test
    void deveRetornarAoFormularioQuandoDadosForemInvalidos() throws Exception {
        mockMvc.perform(post("/agendamentos")
                        .param("clienteId", "")
                        .param("profissionalId", "")
                        .param("_servicoIds", "on")
                        .param("dataHoraInicio", LocalDateTime.now().minusDays(1).format(FORMATO_INPUT)))
                .andExpect(status().isOk())
                .andExpect(view().name("agendamentos/form"))
                .andExpect(model().attributeHasFieldErrors("agendamento", "clienteId", "profissionalId", "servicoIds", "dataHoraInicio"));

        assertThat(agendamentoRepository.count()).isZero();
    }

    @Test
    void formularioDeEdicaoDeveVirPreenchido() throws Exception {
        Agendamento agendamento = salvarAgendamento(inicio, StatusAgendamento.AGENDADO);

        mockMvc.perform(get("/agendamentos/{id}/editar", agendamento.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"" + inicio.format(FORMATO_INPUT) + "\"")))
                .andExpect(content().string(containsString("checked")));
    }

    @Test
    void deveAtualizarAgendamento() throws Exception {
        Agendamento agendamento = salvarAgendamento(inicio, StatusAgendamento.AGENDADO);

        mockMvc.perform(post("/agendamentos/{id}", agendamento.getId())
                        .param("clienteId", cliente.getId().toString())
                        .param("profissionalId", profissional.getId().toString())
                        .param("servicoIds", corte.getId().toString())
                        .param("dataHoraInicio", inicio.plusHours(2).format(FORMATO_INPUT)))
                .andExpect(redirectedUrl("/agendamentos"));

        Agendamento atualizado = agendamentoRepository.findById(agendamento.getId()).orElseThrow();
        assertThat(atualizado.getDataHoraInicio()).isEqualTo(inicio.plusHours(2));
        assertThat(atualizado.getDataHoraFim()).isEqualTo(inicio.plusHours(2).plusMinutes(30));
        assertThat(atualizado.getServicos()).hasSize(1);
    }

    @Test
    void naoDeveAbrirEdicaoDeAgendamentoConcluido() throws Exception {
        Agendamento agendamento = salvarAgendamento(inicio, StatusAgendamento.CONCLUIDO);

        mockMvc.perform(get("/agendamentos/{id}/editar", agendamento.getId()))
                .andExpect(redirectedUrl("/agendamentos"))
                .andExpect(flash().attributeExists("erro"));
    }

    @Test
    void deveCancelarAgendamento() throws Exception {
        Agendamento agendamento = salvarAgendamento(inicio, StatusAgendamento.AGENDADO);

        mockMvc.perform(post("/agendamentos/{id}/cancelar", agendamento.getId()))
                .andExpect(redirectedUrl("/agendamentos"))
                .andExpect(flash().attribute("sucesso", "Agendamento cancelado com sucesso"));

        assertThat(agendamentoRepository.findById(agendamento.getId()).orElseThrow().getStatus())
                .isEqualTo(StatusAgendamento.CANCELADO);
    }

    @Test
    void naoDeveConcluirAgendamentoCancelado() throws Exception {
        Agendamento agendamento = salvarAgendamento(inicio, StatusAgendamento.CANCELADO);

        mockMvc.perform(post("/agendamentos/{id}/concluir", agendamento.getId()))
                .andExpect(redirectedUrl("/agendamentos"))
                .andExpect(flash().attributeExists("erro"));

        assertThat(agendamentoRepository.findById(agendamento.getId()).orElseThrow().getStatus())
                .isEqualTo(StatusAgendamento.CANCELADO);
    }

    private Agendamento salvarAgendamento(LocalDateTime dataHoraInicio, StatusAgendamento status) {
        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setProfissional(profissional);
        agendamento.setServicos(new HashSet<>(Set.of(corte, escova)));
        agendamento.setDataHoraInicio(dataHoraInicio);
        agendamento.recalcularDataHoraFim();
        agendamento.setStatus(status);
        return agendamentoRepository.save(agendamento);
    }

    private Servico salvarServico(String nome, int duracao, String preco) {
        Servico servico = new Servico();
        servico.setNome(nome);
        servico.setDuracaoMinutos(duracao);
        servico.setPreco(new BigDecimal(preco));
        return servicoRepository.save(servico);
    }
}
