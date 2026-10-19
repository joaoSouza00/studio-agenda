package com.salao.studioagenda.controller;

import com.salao.studioagenda.model.Agendamento;
import com.salao.studioagenda.model.Cliente;
import com.salao.studioagenda.model.Profissional;
import com.salao.studioagenda.model.Servico;
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
class CadastrosControllerTest {

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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void paginaInicialDeveCarregar() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("StudioAgenda")));
    }

    @Test
    void deveCadastrarClienteEExibirNaLista() throws Exception {
        mockMvc.perform(post("/clientes")
                        .param("nome", "Maria Silva")
                        .param("telefone", "11999990000")
                        .param("email", "maria@email.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clientes"))
                .andExpect(flash().attribute("sucesso", "Cliente cadastrado com sucesso"));

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Maria Silva")));
    }

    @Test
    void deveRetornarAoFormularioQuandoDadosForemInvalidos() throws Exception {
        mockMvc.perform(post("/clientes")
                        .param("nome", "")
                        .param("telefone", "")
                        .param("email", "invalido"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/form"))
                .andExpect(model().attributeHasFieldErrors("cliente", "nome", "telefone", "email"));

        assertThat(clienteRepository.count()).isZero();
    }

    @Test
    void formularioDeEdicaoDeveVirPreenchido() throws Exception {
        Cliente cliente = salvarCliente();

        mockMvc.perform(get("/clientes/{id}/editar", cliente.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"Maria Silva\"")));
    }

    @Test
    void deveAtualizarServico() throws Exception {
        Servico servico = salvarServico();

        mockMvc.perform(post("/servicos/{id}", servico.getId())
                        .param("nome", "Corte masculino")
                        .param("duracaoMinutos", "40")
                        .param("preco", "60.00"))
                .andExpect(redirectedUrl("/servicos"));

        Servico atualizado = servicoRepository.findById(servico.getId()).orElseThrow();
        assertThat(atualizado.getNome()).isEqualTo("Corte masculino");
        assertThat(atualizado.getDuracaoMinutos()).isEqualTo(40);
        assertThat(atualizado.getPreco()).isEqualByComparingTo("60.00");
    }

    @Test
    void deveExibirPaginaDeErroQuandoRegistroNaoExistir() throws Exception {
        mockMvc.perform(get("/profissionais/{id}/editar", 999))
                .andExpect(status().isNotFound())
                .andExpect(view().name("erro"));
    }

    @Test
    void naoDeveExcluirClienteComAgendamento() throws Exception {
        Cliente cliente = salvarCliente();
        Profissional profissional = new Profissional();
        profissional.setNome("Ana");
        profissional.setEspecialidade("Cabeleireira");
        profissionalRepository.save(profissional);

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setProfissional(profissional);
        agendamento.setServicos(Set.of(salvarServico()));
        agendamento.setDataHoraInicio(LocalDateTime.now().plusDays(1));
        agendamento.recalcularDataHoraFim();
        agendamentoRepository.save(agendamento);

        mockMvc.perform(post("/clientes/{id}/excluir", cliente.getId()))
                .andExpect(redirectedUrl("/clientes"))
                .andExpect(flash().attributeExists("erro"));

        assertThat(clienteRepository.existsById(cliente.getId())).isTrue();
    }

    @Test
    void deveExcluirProfissionalSemAgendamento() throws Exception {
        Profissional profissional = new Profissional();
        profissional.setNome("Ana");
        profissional.setEspecialidade("Manicure");
        profissionalRepository.save(profissional);

        mockMvc.perform(post("/profissionais/{id}/excluir", profissional.getId()))
                .andExpect(redirectedUrl("/profissionais"))
                .andExpect(flash().attribute("sucesso", "Profissional excluído com sucesso"));

        assertThat(profissionalRepository.existsById(profissional.getId())).isFalse();
    }

    private Cliente salvarCliente() {
        Cliente cliente = new Cliente();
        cliente.setNome("Maria Silva");
        cliente.setTelefone("11999990000");
        return clienteRepository.save(cliente);
    }

    private Servico salvarServico() {
        Servico servico = new Servico();
        servico.setNome("Corte");
        servico.setDuracaoMinutos(30);
        servico.setPreco(new BigDecimal("50.00"));
        return servicoRepository.save(servico);
    }
}
