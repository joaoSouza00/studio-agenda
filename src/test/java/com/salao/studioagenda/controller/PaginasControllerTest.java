package com.salao.studioagenda.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class PaginasControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/",
            "/agendamentos",
            "/agendamentos/novo",
            "/clientes",
            "/clientes/novo",
            "/profissionais",
            "/profissionais/novo",
            "/servicos",
            "/servicos/novo"
    })
    void todasAsPaginasDevemCarregarComOLayout(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("navbar-studio")))
                .andExpect(content().string(containsString("bootstrap.bundle.min.js")));
    }
}
